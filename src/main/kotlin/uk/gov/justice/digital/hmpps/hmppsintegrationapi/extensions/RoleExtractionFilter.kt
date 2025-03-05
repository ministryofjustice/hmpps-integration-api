package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.AuthorisationConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.RolesConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuthoriseConsumerService

@Component
@Order(3)
@EnableConfigurationProperties(AuthorisationConfig::class, RolesConfig::class)
class RoleExtractionFilter
  @Autowired
  constructor(
    var authorisationConfig: AuthorisationConfig,
    @Qualifier("data-uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.RolesConfig") var rolesConfig: RolesConfig,
  ) : Filter {
    override fun doFilter(
      request: ServletRequest,
      response: ServletResponse,
      chain: FilterChain,
    ) {
      val subjectDistinguishedName = request.getAttribute("clientName") as String?
      val consumerConfig: ConsumerConfig? = authorisationConfig.consumers[subjectDistinguishedName]
      val req = request as HttpServletRequest
      val res = response as HttpServletResponse
      val authoriseConsumerService = AuthoriseConsumerService()
      val requestedPath = req.requestURI

      val consumersRoles = consumerConfig?.roles
      val roleEndpoints =
        buildList {
          for (consumerRole in consumersRoles.orEmpty()) {
            for (role in rolesConfig.roles) {
              if (role.name == consumerRole) {
                addAll(role.endpoints)
              }
            }
          }
        }

      val result =
        authoriseConsumerService.doesConsumerHaveRoleAccess(roleEndpoints, requestedPath)

      if (!result) {
        res.sendError(HttpServletResponse.SC_FORBIDDEN, "Unable to authorise $requestedPath for $subjectDistinguishedName")
        return
      }

      chain.doFilter(request, response)
    }
  }
