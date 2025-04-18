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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.GlobalsConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.LimitedAccessException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuthoriseConsumerService
import java.io.IOException

@Component
@Order(1)
@EnableConfigurationProperties(AuthorisationConfig::class, GlobalsConfig::class)
class AuthorisationFilter(
  @Autowired val authorisationConfig: AuthorisationConfig,
  @Autowired val globalsConfig: GlobalsConfig,
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
          addAll(globalsConfig.roles[consumerRole]?.include.orEmpty())
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
