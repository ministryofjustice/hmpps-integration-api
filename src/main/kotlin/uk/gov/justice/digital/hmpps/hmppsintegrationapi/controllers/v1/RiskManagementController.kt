package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.AuthorisationConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.RiskManagementPlan

@RestController
@EnableConfigurationProperties(AuthorisationConfig::class)
class RiskManagementController {
  @RequestMapping("/v1/persons/{encodedHmppsId}/risk-management-plan")
  fun getRiskManagementPlans(
    @PathVariable encodedHmppsId: String,
  ): Response<List<RiskManagementPlan>> {
    return Response(emptyList(), emptyList())
  }
}
