package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v2

import io.swagger.v3.oas.annotations.Hidden
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.AuthorisationConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ConfigAuthorisation
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.Role
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.roles

@Hidden
@RestController("ConfigControllerV2")
@EnableConfigurationProperties(AuthorisationConfig::class)
@RequestMapping("/v2/config")
class ConfigController(
  var authorisationConfig: AuthorisationConfig,
) {
  @GetMapping("authorisation")
  fun getAuthorisation(): Map<String, ConfigAuthorisation> = authorisationConfig.consumers.entries.associate { it.key to mapConsumerToIncludesAndFilters(it.value) }

  private fun mapConsumerToIncludesAndFilters(consumerConfig: ConsumerConfig?): ConfigAuthorisation =
    ConfigAuthorisation(
      endpoints = buildEndpointsList(consumerConfig),
      filters = buildFiltersList(consumerConfig),
    )

  private fun buildEndpointsList(consumerConfig: ConsumerConfig?): List<String> =
    buildList {
      for (consumerRole in consumerConfig?.roles.orEmpty()) {
        addAll(roles[consumerRole]?.include.orEmpty())
      }
      addAll(consumerConfig?.include.orEmpty())
    }

  private fun buildFiltersList(consumerConfig: ConsumerConfig?): ConsumerFilters? {
    val aggregatedRoles: List<Role>? = consumerConfig?.roles?.mapNotNull { roles[it] }
    val consumerPseudoRole = Role(include = null, filters = consumerConfig?.filters)
    val allRoles: List<Role> = listOf(consumerPseudoRole) + (aggregatedRoles ?: emptyList())

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
}
