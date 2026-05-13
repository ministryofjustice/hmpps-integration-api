package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.AuthorisationConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.LimitedAccessException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.AuthorisationService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.OboService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuthoriseConsumerService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.RoleService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.telemetry.TelemetryService
import java.io.IOException

@Component
@EnableConfigurationProperties(AuthorisationConfig::class)
class AuthorisationFilter(
  private val authorisationService: AuthorisationService,
  private val telemetryService: TelemetryService,
  private val roleService: RoleService,
  private val oboService: OboService,
) : Filter {
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

    // Get the on behalf of token
    val onBehalfOf = req.getHeader("X-On-Behalf-Of")

    val decodedJwt = oboService.decodeJwt(onBehalfOf)

    // capture token if decoded
    if (decodedJwt != null) {
      telemetryService.captureMessage(
        "Token found with iss:${decodedJwt["iss"]}, " +
          "appId:${decodedJwt["appid"]}, " +
          "unique_name:${decodedJwt["unique_name"]}, " +
          "kid:${decodedJwt["kid"]}, " +
          "nbf:${decodedJwt["nbf"]}, " +
          "exp:${decodedJwt["exp"]}, " +
          "aud:${decodedJwt["aud"]}",
      )
    }

    // Set App insights request attributes
    setSpanAttributes(clientName, certificateSerialNumber, onBehalfOf, decodedJwt?.get("unique_name") as String?)

    // Set filters
    val consumerConfig: ConsumerConfig? = authorisationService.consumers()[clientName]

    if (consumerConfig == null) {
      response.sendError(HttpServletResponse.SC_FORBIDDEN, "No consumer authorisation config found for $clientName")
      return
    }

    // Authorise request

    val filters = authorisationService.allFilters(clientName)
    request.setAttribute("filters", filters)

    val authoriseConsumerService = AuthoriseConsumerService()
    val requestedPath = req.requestURI

    if (authorisedThroughIncludes(authoriseConsumerService, clientName, requestedPath) ||
      authorisedThroughRole(authoriseConsumerService, clientName, requestedPath)
    ) {
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

  private fun authorisedThroughRole(
    authoriseConsumerService: AuthoriseConsumerService,
    consumerName: String?,
    requestedPath: String,
  ): Boolean {
    val consumerConfig: ConsumerConfig? = authorisationService.consumers()[consumerName]
    val consumersRoles = consumerConfig?.roles
    val rolesInclude =
      buildList {
        for (consumerRole in consumersRoles.orEmpty()) {
          addAll(roleService.getRoles()[consumerRole]?.permissions.orEmpty())
        }
      }
    val roleResult =
      authoriseConsumerService.doesConsumerHaveRoleAccess(rolesInclude, requestedPath)
    return roleResult
  }

  private fun authorisedThroughIncludes(
    authoriseConsumerService: AuthoriseConsumerService,
    consumerName: String?,
    requestedPath: String,
  ) = authoriseConsumerService.doesConsumerHaveIncludesAccess(authorisationService.consumers()[consumerName], requestedPath)

  private fun setSpanAttributes(
    clientId: String,
    certSerialNumber: String?,
    onBehalfOf: String?,
    uniqueName: String?,
  ) {
    telemetryService.setSpanAttribute("clientId", clientId)
    certSerialNumber?.let { telemetryService.setSpanAttribute("certSerialNumber", certSerialNumber) }
    onBehalfOf?.let { telemetryService.setSpanAttribute("onBehalfOf", it) }
    uniqueName?.let { telemetryService.setSpanAttribute("uniqueName", it) }
  }
}
