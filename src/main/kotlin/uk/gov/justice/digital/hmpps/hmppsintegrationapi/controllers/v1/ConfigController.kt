package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1

import io.swagger.v3.oas.annotations.Hidden
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.AuthorisationConfig

@Hidden
@RestController
@EnableConfigurationProperties(AuthorisationConfig::class)
@RequestMapping("/v1/config")
class ConfigController(
  var authorisationConfig: AuthorisationConfig,
) {
  @GetMapping("authorisation")
  fun getAuthorisation(): Map<String, List<String>> {
    return authorisationConfig.consumers
  }
}
