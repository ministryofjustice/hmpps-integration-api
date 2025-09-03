package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.AuthorisationConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.GlobalsConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.Role
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import java.io.IOException

@Component
@Order(2)
@EnableConfigurationProperties(AuthorisationConfig::class, GlobalsConfig::class)
class FiltersExtractionFilter
  @Autowired
  constructor(
    var authorisationConfig: AuthorisationConfig,
    val globalsConfig: GlobalsConfig,
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

      val aggregatedRoles: List<Role>? = consumerConfig.roles?.mapNotNull { globalsConfig.roles[it] }

      val filters = buildAggregatedFilters(aggregatedRoles)
      request.setAttribute("filters", filters)
      chain.doFilter(request, response)
    }
  }

private fun buildAggregatedFilters(roles: List<Role>?): ConsumerFilters? {
  if (roles == null || roles.isEmpty() || (roles.all { it.filters == null })) return null

  val prisons =
    roles
      .takeIf { role -> role.any { it.filters?.prisons != null } }
      ?.mapNotNull { it.filters?.prisons }
      ?.flatten()
      ?.distinct()

  val caseNotes =
    roles
      .takeIf { role -> role.any { it.filters?.caseNotes != null } }
      ?.mapNotNull { it.filters?.caseNotes }
      ?.flatten()
      ?.distinct()

  return ConsumerFilters(prisons, caseNotes)
}
