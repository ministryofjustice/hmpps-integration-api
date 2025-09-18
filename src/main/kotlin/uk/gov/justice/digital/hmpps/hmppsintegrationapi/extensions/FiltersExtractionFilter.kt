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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.USE_ROLES_DSL
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.GlobalsConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.Role
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.roles
import java.io.IOException

@Component
@Order(2)
@EnableConfigurationProperties(AuthorisationConfig::class, GlobalsConfig::class)
class FiltersExtractionFilter
  @Autowired
  constructor(
    var authorisationConfig: AuthorisationConfig,
    val globalsConfig: GlobalsConfig,
    val featureFlagConfig: FeatureFlagConfig?,
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

      val aggregatedRoles: List<Role>? =
        consumerConfig.roles?.mapNotNull {
          if (featureFlagConfig?.isEnabled(USE_ROLES_DSL) == true) {
            roles[it]
          } else {
            globalsConfig.roles[it]
          }
        }
      val filters = buildAggregatedFilters(consumerConfig.filters, aggregatedRoles)
      request.setAttribute("filters", filters)
      chain.doFilter(request, response)
    }
  }

private fun buildAggregatedFilters(
  consumerFilters: ConsumerFilters?,
  roles: List<Role>?,
): ConsumerFilters? {
  val consumerPseudoRole = Role(include = null, filters = consumerFilters)
  val allRoles: List<Role> = listOf(consumerPseudoRole) + (roles ?: emptyList())

  if (allRoles.all { it.filters?.hasFilters() == false }) {
    return null
  }

  val prisons =
    getDistinctValuesIfNotWildcarded(
      allRoles
        .filter { it.filters?.hasPrisonFilter() == true }
        .mapNotNull { it.filters?.prisons },
    )

  val caseNotes =
    getDistinctValuesIfNotWildcarded(
      allRoles
        .filter { it.filters?.hasCaseNotesFilter() == true }
        .mapNotNull { it.filters?.caseNotes },
    )

  return if (caseNotes == null && prisons == null) null else ConsumerFilters(prisons, caseNotes)
}

private fun getDistinctValuesIfNotWildcarded(allValues: List<List<String>>): List<String>? =
  if (allValues.isEmpty()) {
    null
  } else {
    allValues.flatten().distinct().takeIf { it.none { value -> value == "*" } }
  }
