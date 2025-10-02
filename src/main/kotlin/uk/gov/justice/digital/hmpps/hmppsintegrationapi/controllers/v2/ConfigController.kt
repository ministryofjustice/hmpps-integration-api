package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v2

import io.swagger.v3.oas.annotations.Hidden
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.AuthorisationConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ConfigAuthorisation

@Hidden
@RestController("ConfigControllerV2")
@EnableConfigurationProperties(AuthorisationConfig::class)
@RequestMapping("/v2/config")
class ConfigController(
  var authorisationConfig: AuthorisationConfig,
) {
  @GetMapping("authorisation")
  fun getAuthorisation(): Map<String, ConfigAuthorisation> = authorisationConfig.consumers.entries.associate { it.key to mapConsumerToIncludesAndFilters(it.key) }

  private fun mapConsumerToIncludesAndFilters(consumerName: String): ConfigAuthorisation =
    ConfigAuthorisation(
      endpoints = authorisationConfig.allIncludes(consumerName),
      filters = authorisationConfig.allFilters(consumerName),
    )
}
