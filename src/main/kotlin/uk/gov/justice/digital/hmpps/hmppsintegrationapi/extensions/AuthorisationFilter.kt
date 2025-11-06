package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.AuthorisationConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.LimitedAccessException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.roles
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuthoriseConsumerService
import java.io.IOException

@Component
@Order(1)
@EnableConfigurationProperties(AuthorisationConfig::class)
class AuthorisationFilter(
  @Autowired val authorisationConfig: AuthorisationConfig,
  @Autowired val featureFlagConfig: FeatureFlagConfig,
) : Filter {
  @Throws(IOException::class, ServletException::class)
  override fun doFilter(
    request: ServletRequest,
    response: ServletResponse?,
    chain: FilterChain,
  ) {
    val req = request as HttpServletRequest
    val res = response as HttpServletResponse

    val subjectDistinguishedName = req.getAttribute("clientName") as String?
    if (subjectDistinguishedName == null) {
      res.sendError(HttpServletResponse.SC_FORBIDDEN, "No subject-distinguished-name header provided for authorisation")
      return
    }

    if (featureFlagConfig.isEnabled(FeatureFlagConfig.CERT_REVOCATION_ENABLED)) {
      val certificateSerialNumber = req.getAttribute("certificateSerialNumber") as String?
      if (certificateSerialNumber != null && certificateRevoked(authorisationConfig, certificateSerialNumber, subjectDistinguishedName)) {
        res.sendError(HttpServletResponse.SC_FORBIDDEN, "Certificate with serial number $certificateSerialNumber has been revoked")
        return
      }
    }

    val authoriseConsumerService = AuthoriseConsumerService()
    val requestedPath = req.requestURI

    if (authorisedThroughIncludes(authoriseConsumerService, subjectDistinguishedName, requestedPath) ||
      authorisedThroughRole(authoriseConsumerService, subjectDistinguishedName, requestedPath)
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
      res.sendError(HttpServletResponse.SC_FORBIDDEN, "Unable to authorise $requestedPath for $subjectDistinguishedName")
    }
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
    authorisationConfig: AuthorisationConfig,
    certificateSerialNumber: String,
    consumerName: String,
  ): Boolean {
    authorisationConfig.certificateRevocationList.forEach {
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
    subjectDistinguishedName: String?,
    requestedPath: String,
  ): Boolean {
    val consumerConfig: ConsumerConfig? = authorisationConfig.consumers[subjectDistinguishedName]
    val consumersRoles = consumerConfig?.roles
    val rolesInclude =
      buildList {
        for (consumerRole in consumersRoles.orEmpty()) {
          addAll(roles[consumerRole]?.permissions.orEmpty())
        }
      }
    val roleResult =
      authoriseConsumerService.doesConsumerHaveRoleAccess(rolesInclude, requestedPath)
    return roleResult
  }

  private fun authorisedThroughIncludes(
    authoriseConsumerService: AuthoriseConsumerService,
    subjectDistinguishedName: String?,
    requestedPath: String,
  ) = authoriseConsumerService.doesConsumerHaveIncludesAccess(authorisationConfig.consumers[subjectDistinguishedName], requestedPath)
}
