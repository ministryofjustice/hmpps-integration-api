package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1

import io.swagger.v3.oas.annotations.Hidden
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.AuthorisationConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.GlobalsConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerConfig

@Hidden
@RestController
@EnableConfigurationProperties(AuthorisationConfig::class, GlobalsConfig::class)
@RequestMapping("/v1/config")
class ConfigController(
  var authorisationConfig: AuthorisationConfig,
  var globalsConfig: GlobalsConfig,
) {
  @GetMapping("authorisation")
  fun getAuthorisation(): Map<String, List<String>> = authorisationConfig.consumers.entries.associate { it.key to mapConsumerToIncludes(it.value) }

  private fun mapConsumerToIncludes(consumerConfig: ConsumerConfig?): List<String> =
    buildList {
      for (consumerRole in consumerConfig?.roles.orEmpty()) {
        addAll(globalsConfig.roles[consumerRole]?.include.orEmpty())
      }
      addAll(consumerConfig?.include.orEmpty())
    }
}
