package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.AuthorisationConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.mergeFeatures
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.LimitedAccessException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.AuthorisationService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.telemetry.TelemetryService
import java.io.IOException

@Component
@EnableConfigurationProperties(AuthorisationConfig::class)
class AuthorisationFilter(
  private val authorisationService: AuthorisationService,
  private val telemetryService: TelemetryService,
  private val features: FeatureFlagConfig,
) : Filter {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @Throws(IOException::class, ServletException::class)
  override fun doFilter(
    request: ServletRequest,
    response: ServletResponse?,
    chain: FilterChain,
  ) {
    val req = request as HttpServletRequest
    val res = response as HttpServletResponse

    // Get the consumer Name from the SDN
    val subjectDistinguishedName = req.getHeader("subject-distinguished-name")
    val clientName = extractConsumerName(subjectDistinguishedName)

    if (clientName == null) {
      res.sendError(HttpServletResponse.SC_FORBIDDEN, "No subject-distinguished-name header provided for authorisation")
      return
    }
    // Set the client name in the request
    req.setAttribute("clientName", clientName)

    // Get the cert serial number
    val certificateSerialNumber = extractCertificateSerialNumber(req.getHeader("cert-serial-number"))
    if (certificateSerialNumber != null && certificateRevoked(authorisationService.certificateRevocationList(), certificateSerialNumber, clientName)) {
      res.sendError(HttpServletResponse.SC_FORBIDDEN, "Certificate with serial number $certificateSerialNumber has been revoked")
      return
    }

    // Get certificate expiry date
    val certificateExpiryDate = req.getHeader("cert-expiry-date")?.let { authorisationService.processCertificateExpiryDate(it, clientName) }

    // Get the on behalf of token
    val onBehalfOf = req.getHeader("X-On-Behalf-Of")

    val oboUsername =
      onBehalfOf?.let {
        val oboService = authorisationService.oboService(clientName)
        oboService?.extractUsername(it)
      }

    // Set App insights request attributes
    setSpanAttributes(clientName, certificateSerialNumber, oboUsername ?: onBehalfOf, certificateExpiryDate)

    if (authorisationService.requiresObo(clientName)) {
      if (oboUsername.isNullOrEmpty()) {
        res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "On Behalf Of username unavailable for $clientName")
        return
      }

      if (!authorisationService.verifyUsername(oboUsername, clientName)) {
        log.error("On Behalf Of username: $oboUsername not found in hmpps auth")
        res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not authorized")
        return
      }
    }

    // Set filters
    val consumerConfig: ConsumerConfig? = authorisationService.consumers()[clientName]

    if (consumerConfig == null) {
      response.sendError(HttpServletResponse.SC_FORBIDDEN, "No consumer authorisation config found for $clientName")
      return
    }

    val requestFeatures = featuresWithOverrides(features, consumerConfig, request.getHeader("X-Feature-Override"))

    // Authorise request

    val filters = authorisationService.allFilters(clientName)
    request.setAttribute("filters", filters)

    val requestedPath = req.requestURI

    val context = RequestContext(clientName, consumerConfig, filters, requestFeatures, oboUsername)
    request.setAttribute("requestContext", context)

    if (authorisationService.hasAccess(clientName, requestedPath)) {
      try {
        chain.doFilter(request, response)
      } catch (e: Throwable) {
        val cause = e.cause
        if (cause is LimitedAccessException) {
          res.sendError(HttpServletResponse.SC_FORBIDDEN, cause.message)
        } else {
          throw e
        }
      }
      return
    } else {
      res.sendError(HttpServletResponse.SC_FORBIDDEN, "Unable to authorise $requestedPath for $clientName")
    }
  }

  /**
   * Override feature flags based on request headers if the consumer is permitted to do this.
   *
   * If not permitted, returns the original feature config.
   *
   * The original feature config is not modified in either case.
   */
  internal fun featuresWithOverrides(
    environmentFeatures: FeatureFlagConfig,
    consumerConfig: ConsumerConfig,
    overrides: String?,
  ): FeatureFlagConfig =
    if (consumerConfig.allowFeatureOverride) {
      mergeFeatures(environmentFeatures, overrides)
    } else {
      environmentFeatures
    }

  fun extractConsumerName(subjectDistinguishedName: String?): String? {
    if (subjectDistinguishedName.isNullOrEmpty()) {
      // Return the profiles default consumer name or return null
      // Default consumer name should only be set for the local profile
      return authorisationService.defaultConsumerName()
    }
    val match = Regex("^.*,CN=(.*)$").find(subjectDistinguishedName)

    if (match?.groupValues == null) {
      return null
    }
    return match.groupValues[1]
  }

  /**
   * Converts the certificate serial number sent in the header into hex format
   * e.g 9572494320151578633330348943480876283449388176
   * becomes 01:7B:EB:77:06:DB:11:F5:2E:B6:F7:37:7B:A9:E0:E4:84:C5:2C:A3
   */
  fun extractCertificateSerialNumber(serialNumber: String?): String? =
    serialNumber?.let {
      runCatching {
        serialNumber.toBigInteger().toByteArray().toHexString(
          format =
            HexFormat {
              upperCase = true
              bytes.byteSeparator = ":"
            },
        )
      }.getOrNull()
    }

  /**
   * Checks whether the certificate serial number exists in the certificate revocation list in application.yaml
   * If the entry contains a "/" then the entry only applies to the consumer name that follows the "/"
   * e.g for these 2 entries in application.yaml
   * authorisation:
   *  certificate-revocation-list:
   *    - 01:7b:eb:77:06:db:11:f5:2e:b6:f7:37:7b:a9:e0:e4:84:c5:2c:a3
   *    - 01/a-consumer
   *
   * The first entry would apply globally. The second entry would only apply to a consumer with name a-consumer
   */
  fun certificateRevoked(
    certificateRevocationList: List<String>,
    certificateSerialNumber: String,
    consumerName: String,
  ): Boolean {
    certificateRevocationList.forEach {
      val entry = it.split("/")
      val serialNumber = entry[0]
      val thisConsumerOnly = if (entry.size > 1) entry[1] else null
      if (thisConsumerOnly != null) {
        if (serialNumber.equals(certificateSerialNumber, ignoreCase = true) && thisConsumerOnly == consumerName) {
          return true
        }
      } else {
        if (serialNumber.equals(certificateSerialNumber, ignoreCase = true)) {
          return true
        }
      }
    }
    return false
  }

  private fun setSpanAttributes(
    clientId: String,
    certSerialNumber: String?,
    onBehalfOf: String?,
    certExpiryDate: String?,
  ) {
    telemetryService.setSpanAttribute("clientId", clientId)
    certSerialNumber?.let { telemetryService.setSpanAttribute("certSerialNumber", certSerialNumber) }
    certExpiryDate?.let { telemetryService.setSpanAttribute("certExpiryDate", certExpiryDate) }
    onBehalfOf?.let { telemetryService.setSpanAttribute("onBehalfOf", onBehalfOf) }
  }
}
