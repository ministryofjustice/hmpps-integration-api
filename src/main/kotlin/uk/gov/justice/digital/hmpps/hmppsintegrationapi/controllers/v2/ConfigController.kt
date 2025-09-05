package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v2

import io.swagger.v3.oas.annotations.Hidden
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.AuthorisationConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.GlobalsConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ConfigAuthorisation
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.Role
import kotlin.collections.orEmpty

@Hidden
@RestController("ConfigControllerV2")
@EnableConfigurationProperties(AuthorisationConfig::class, GlobalsConfig::class)
@RequestMapping("/v2/config")
class ConfigController(
  var authorisationConfig: AuthorisationConfig,
  var globalsConfig: GlobalsConfig,
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
        addAll(globalsConfig.roles[consumerRole]?.include.orEmpty())
      }
      addAll(consumerConfig?.include.orEmpty())
    }

  private fun buildFiltersList(consumerConfig: ConsumerConfig?): ConsumerFilters? {
    val aggregatedRoles: List<Role>? = consumerConfig?.roles?.mapNotNull { globalsConfig.roles[it] }
    if (aggregatedRoles == null || aggregatedRoles.isEmpty() || (aggregatedRoles.all { it.filters == null })) return consumerConfig?.filters

    val consumerPseudoRole = Role(include = consumerConfig.include, filters = consumerConfig.filters)

    val prisons: List<String>? =
      (aggregatedRoles + listOf(consumerPseudoRole))
        .takeIf { role -> role.any { it.filters?.prisons != null } }
        ?.mapNotNull { it.filters?.prisons }
        ?.flatten()
        ?.distinct()

    val caseNotes: List<String>? =
      (aggregatedRoles + listOf(consumerPseudoRole))
        .takeIf { role -> role.any { it.filters?.caseNotes != null } }
        ?.mapNotNull { it.filters?.caseNotes }
        ?.flatten()
        ?.distinct()

    return ConsumerFilters(prisons, caseNotes)
  }
}
