package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.AuthorisationConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.RolesConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerConfig

@Component
@Order(3)
@EnableConfigurationProperties(AuthorisationConfig::class, RolesConfig::class)
class RoleExtractionFilter
  @Autowired
  constructor(
    var authorisationConfig: AuthorisationConfig,
    @Qualifier("data-uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.RolesConfig") var roles: RolesConfig,
  ) : Filter {
    override fun doFilter(
      request: ServletRequest,
      response: ServletResponse,
      chain: FilterChain,
    ) {
      val subjectDistinguishedName = request.getAttribute("clientName") as String?
      val consumerConfig: ConsumerConfig? = authorisationConfig.consumers[subjectDistinguishedName]

      val consumersRoles = consumerConfig?.roles
      val roleEndpoints =
        buildList {
          for (consumerRole in consumersRoles.orEmpty()) {
            for (role in roles.roles) {
              if (role.name == consumerRole) {
                addAll(role.endpoints)
              }
            }
          }
        }

      request.setAttribute("roleEndpoints", roleEndpoints)
      chain.doFilter(request, response)
    }
  }
