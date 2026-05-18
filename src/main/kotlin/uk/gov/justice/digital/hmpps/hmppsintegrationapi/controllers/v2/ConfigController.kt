package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v2

import io.swagger.v3.oas.annotations.Hidden
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ConfigAuthorisation
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuthorisationService

@Hidden
@RestController("ConfigControllerV2")
@RequestMapping("/v2/config")
class ConfigController(
  var authorisationService: AuthorisationService,
) {
  @GetMapping("authorisation")
  fun getAuthorisation(): Map<String, ConfigAuthorisation> = authorisationService.consumers().entries.associate { it.key to mapConsumerToIncludesAndFilters(it.key) }

  private fun mapConsumerToIncludesAndFilters(consumerName: String): ConfigAuthorisation =
    ConfigAuthorisation(
      endpoints = authorisationService.allPermissions(consumerName),
      filters = authorisationService.allFilters(consumerName),
    )
}
