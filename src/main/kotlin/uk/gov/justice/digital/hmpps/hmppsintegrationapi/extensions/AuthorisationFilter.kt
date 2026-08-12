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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.CertificateService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.telemetry.TelemetryService
import java.io.IOException
import java.time.Instant

@Component
@EnableConfigurationProperties(AuthorisationConfig::class)
class AuthorisationFilter(
  private val authorisationService: AuthorisationService,
  private val telemetryService: TelemetryService,
  private val features: FeatureFlagConfig,
  private val certificateService: CertificateService,
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

    val certInfo = certificateService.getCertificateInfo(clientName, req.getHeader("cert-serial-number"), req.getHeader("cert-expiry-date"))

    if (certInfo.isRevoked) {
      res.sendError(HttpServletResponse.SC_FORBIDDEN, "Certificate with serial number ${certInfo.seriaNumber} has been revoked")
      return
    }

    // Get the on behalf of token
    val onBehalfOf = req.getHeader("X-On-Behalf-Of")

    val oboUsername =
      onBehalfOf?.let {
        val oboService = authorisationService.oboService(clientName)
        oboService?.extractUsername(it)
      }

    val consumerConfig: ConsumerConfig? = authorisationService.consumers()[clientName]

    val featureOverrides = request.getHeader("X-Feature-Override")
    val requestFeatures = featuresWithOverrides(features, consumerConfig, featureOverrides)

    // Set App insights request attributes
    setSpanAttributes(clientName, certInfo.seriaNumber, oboUsername ?: onBehalfOf, certInfo.expiresAt, featureOverrides)

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

    if (consumerConfig == null) {
      response.sendError(HttpServletResponse.SC_FORBIDDEN, "No consumer authorisation config found for $clientName")
      return
    }

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
    consumerConfig: ConsumerConfig?,
    overrides: String?,
  ): FeatureFlagConfig =
    if (consumerConfig?.allowFeatureOverride ?: false) {
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
    return certificateService.extractConsumerName(subjectDistinguishedName)
  }

  private fun setSpanAttributes(
    clientId: String,
    certSerialNumber: String?,
    onBehalfOf: String?,
    certExpiryDate: Instant?,
    featureOverrides: String?,
  ) {
    telemetryService.setSpanAttribute("clientId", clientId)
    certSerialNumber?.let { telemetryService.setSpanAttribute("certSerialNumber", certSerialNumber) }
    certExpiryDate?.let { telemetryService.setSpanAttribute("certExpiryDate", certExpiryDate.toString()) }
    onBehalfOf?.let { telemetryService.setSpanAttribute("onBehalfOf", onBehalfOf) }
    featureOverrides?.let { telemetryService.setSpanAttribute("featureOverrides", featureOverrides) }
  }
}
