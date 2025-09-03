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

  private fun buildFiltersList(consumerConfig: ConsumerConfig?): ConsumerFilters {
    val aggregatedPrisonFilters: List<String>? =
      consumerConfig
        ?.roles
        ?.mapNotNull { consumerRole ->
          globalsConfig.roles[consumerRole]?.filters?.prisons
        }?.flatten()
        ?.takeIf { it.isNotEmpty() }

    val aggregatedCaseNoteFilters: List<String>? =
      consumerConfig
        ?.roles
        ?.mapNotNull { consumerRole ->
          globalsConfig.roles[consumerRole]?.filters?.caseNotes
        }?.flatten()
        ?.takeIf { it.isNotEmpty() }

    return ConsumerFilters(aggregatedPrisonFilters, aggregatedCaseNoteFilters)
  }
}
