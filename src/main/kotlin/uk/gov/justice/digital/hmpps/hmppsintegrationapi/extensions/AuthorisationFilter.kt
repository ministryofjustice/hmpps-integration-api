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
import org.springframework.context.annotation.Profile
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
@Order(0)
@Profile("!local")
class ConsumerNameExtractionFilter : Filter {
  @Throws(IOException::class, ServletException::class)
  override fun doFilter(
    request: ServletRequest,
    response: ServletResponse?,
    chain: FilterChain,
  ) {
    val req = request as HttpServletRequest
    response as HttpServletResponse
    val subjectDistinguishedName = req.getHeader("subject-distinguished-name")
    val certificateSerialNumber = extractCertificateSerialNumber(req.getHeader("cert-serial-number"))
    val extractedConsumerName = extractConsumerName(subjectDistinguishedName)
    req.setAttribute("clientName", extractedConsumerName)
    req.setAttribute("certificateSerialNumber", certificateSerialNumber)
    chain.doFilter(request, response)
  }

  fun extractConsumerName(subjectDistinguishedName: String?): String? {
    if (subjectDistinguishedName.isNullOrEmpty()) {
      return null
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
}

@Component
@Order(0)
@Profile("local")
class LocalConsumerNameExtractionFilter : Filter {
  override fun doFilter(
    request: ServletRequest,
    response: ServletResponse?,
    chain: FilterChain,
  ) {
    chain.doFilter(request.apply { setAttribute("clientName", "all-access") }, response)
  }
}

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

    val certificateSerialNumber = req.getAttribute("certificateSerialNumber") as String?
    if (certificateSerialNumber != null && certificateRevoked(authorisationConfig, certificateSerialNumber, subjectDistinguishedName)) {
      res.sendError(HttpServletResponse.SC_FORBIDDEN, "Certificate with serial number $certificateSerialNumber has been revoked")
      return
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

@Component
@Order(2)
@EnableConfigurationProperties(AuthorisationConfig::class)
class FiltersExtractionFilter
  @Autowired
  constructor(
    var authorisationConfig: AuthorisationConfig,
  ) : Filter {
    @Throws(IOException::class, ServletException::class)
    override fun doFilter(
      request: ServletRequest,
      response: ServletResponse,
      chain: FilterChain,
    ) {
      val subjectDistinguishedName = request.getAttribute("clientName") as String?
      val consumerConfig: ConsumerConfig? = authorisationConfig.consumers[subjectDistinguishedName]

      if (consumerConfig == null) {
        (response as HttpServletResponse).sendError(HttpServletResponse.SC_FORBIDDEN, "No consumer authorisation config found for $subjectDistinguishedName")
        return
      }

      val filters = authorisationConfig.allFilters(subjectDistinguishedName!!)
      request.setAttribute("filters", filters)
      chain.doFilter(request, response)
    }
  }
