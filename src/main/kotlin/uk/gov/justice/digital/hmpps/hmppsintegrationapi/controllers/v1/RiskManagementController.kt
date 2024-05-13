package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.AuthorisationConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.decodeUrlCharacters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.RiskManagementPlan
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetRiskManagementPlansForCrnService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.PaginatedResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.paginateWith

@RestController
@EnableConfigurationProperties(AuthorisationConfig::class)
class RiskManagementController (
  @Autowired val getRiskManagementPlansForCrnService: GetRiskManagementPlansForCrnService,
  @Autowired val auditService: AuditService
) {

  @RequestMapping("/v1/persons/{encodedHmppsId}/risk-management-plan")
  fun getRiskManagementPlans(
    @PathVariable encodedHmppsId: String,
    @RequestParam(required = false, defaultValue = "1", name = "page") page: Int,
    @RequestParam(required = false, defaultValue = "10", name = "perPage") perPage: Int,
  ): PaginatedResponse<RiskManagementPlan> {
    val hmppsId = encodedHmppsId.decodeUrlCharacters()
    val response = getRiskManagementPlansForCrnService.execute(hmppsId)
    if (response.data.isNullOrEmpty()) {
      if (response.hasErrorCausedBy(UpstreamApiError.Type.ENTITY_NOT_FOUND, causedBy = UpstreamApi.RISK_MANAGEMENT_PLAN)) {
        throw EntityNotFoundException("Could not find person with id: $hmppsId")
      }
      return emptyList<RiskManagementPlan>().paginateWith(page, perPage)
    }

    auditService.createEvent("GET_RISK_MANAGEMENT_PLANS", mapOf("hmppsId" to hmppsId))
    return response.data.paginateWith(page, perPage)
  }

}
