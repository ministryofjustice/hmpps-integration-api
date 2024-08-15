package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.OpenAPIConfig.Companion.HMPPS_ID
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.OpenAPIConfig.Companion.PAGE
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.OpenAPIConfig.Companion.PER_PAGE
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.decodeUrlCharacters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.RiskPredictorScore
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetRiskPredictorScoresForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.PaginatedResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.paginateWith

@RestController
@RequestMapping("/v1/persons")
@Tag(name = "risks")
class RiskPredictorScoresController(
  @Autowired val getRiskPredictorScoresForPersonService: GetRiskPredictorScoresForPersonService,
  @Autowired val auditService: AuditService,
) {
  @GetMapping("{encodedHmppsId}/risks/scores")
  @Operation(
    summary = "Returns risk scores from the last year associated with a person, sorted by completedDate (newest first). This endpoint does not serve LAO (Limited Access Offender) data.",
    responses = [
      ApiResponse(responseCode = "200", description = "Successfully found risk scores for a person with the provided HMPPS ID."),
      ApiResponse(responseCode = "404", content = [Content(schema = Schema(ref = "#/components/schemas/PersonNotFound"))]),
      ApiResponse(responseCode = "500", content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))]),
    ],
  )
  fun getPersonRiskPredictorScores(
    @Parameter(ref = HMPPS_ID) @PathVariable encodedHmppsId: String,
    @Parameter(ref = PAGE) @RequestParam(required = false, defaultValue = "1", name = "page") page: Int,
    @Parameter(ref = PER_PAGE) @RequestParam(required = false, defaultValue = "10", name = "perPage") perPage: Int,
  ): PaginatedResponse<RiskPredictorScore> {
    val hmppsId = encodedHmppsId.decodeUrlCharacters()
    val response = getRiskPredictorScoresForPersonService.execute(hmppsId)

    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not find person with id: $hmppsId")
    }
    auditService.createEvent("GET_PERSON_RISK_SCORES", mapOf("hmppsId" to hmppsId))
    return response.data.paginateWith(page, perPage)
  }
}
