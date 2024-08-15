package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.AuthorisationConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.OpenAPIConfig.Companion.HMPPS_ID
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.OpenAPIConfig.Companion.PAGE
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.OpenAPIConfig.Companion.PER_PAGE
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
@Tag(name = "risks")
class RiskManagementController(
  @Autowired val getRiskManagementPlansForCrnService: GetRiskManagementPlansForCrnService,
  @Autowired val auditService: AuditService,
) {
  @GetMapping("/v1/persons/{encodedHmppsId}/risk-management-plan")
  @Operation(
    summary = "Returns a list of Risk Management Plans created for the person with the provided HMPPS ID.",
    responses = [
      ApiResponse(responseCode = "200", description = "Successfully found risk management plans for a person with the provided HMPPS ID."),
      ApiResponse(responseCode = "404", content = [Content(schema = Schema(ref = "#/components/schemas/PersonNotFound"))]),
      ApiResponse(responseCode = "500", content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))]),
    ],
  )
  fun getRiskManagementPlans(
    @Parameter(ref = HMPPS_ID) @PathVariable encodedHmppsId: String,
    @Parameter(ref = PAGE) @RequestParam(required = false, defaultValue = "1", name = "page") page: Int,
    @Parameter(ref = PER_PAGE) @RequestParam(required = false, defaultValue = "10", name = "perPage") perPage: Int,
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
