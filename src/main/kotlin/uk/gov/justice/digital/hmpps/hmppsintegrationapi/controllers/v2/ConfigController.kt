package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v2

import io.swagger.v3.oas.annotations.Hidden
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.AuthorisationConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ConfigAuthorisation
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerConfig
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

  private fun mapConsumerToIncludesAndFilters(consumerConfig: ConsumerConfig?): ConfigAuthorisation {
    val aggregatedRoles: List<Role>? = consumerConfig?.roles?.mapNotNull { roles[it] }
    return ConfigAuthorisation(
      endpoints = buildEndpointsList(consumerConfig),
      filters = authorisationConfig.buildAggregatedFilters(consumerConfig?.filters, aggregatedRoles),
    )
  }

  private fun buildEndpointsList(consumerConfig: ConsumerConfig?): List<String> =
    buildList {
      for (consumerRole in consumerConfig?.roles.orEmpty()) {
        addAll(roles[consumerRole]?.include.orEmpty())
      }
      addAll(consumerConfig?.include.orEmpty())
    }
}
