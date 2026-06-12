package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.tags.Tags
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.featureflag.FeatureFlag
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.AssessmentSummary
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DataResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.AssessmentSummaryService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

@RestController
@RequestMapping("/v1/persons")
@Tags(value = [Tag(name = "Persons"), Tag(name = "Assessment Summary")])
class AssessmentSummaryController(
  @Autowired val assessmentSummaryService: AssessmentSummaryService,
  @Autowired val auditService: AuditService,
) {
  @GetMapping("{hmppsId}/assessment-summary")
  @Operation(
    summary = "Returns ",
    responses = [
      ApiResponse(responseCode = "200", useReturnTypeSchema = true, description = "Successfully found the most recent completed assessment for the provided HMPPS ID."),
      ApiResponse(responseCode = "404", content = [Content(schema = Schema(ref = "#/components/schemas/PersonNotFound"))]),
      ApiResponse(responseCode = "500", content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))]),
    ],
  )
  @FeatureFlag(name = FeatureFlagConfig.USE_ASSESSMENT_SUMMARY_ENDPOINT)
  fun getAssessmentSummary(
    @Parameter(description = "HMPPS identifier", example = "A1234AA") @PathVariable hmppsId: String,
  ): DataResponse<AssessmentSummary?> {
    val response = assessmentSummaryService.assessmentSummary(hmppsId)

    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not find an assessment summary for person with id: $hmppsId")
    }
    auditService.createEvent("GET_PERSON_ASSESSMENT_SUMMARY", mapOf("hmppsId" to hmppsId))
    return DataResponse(response.data)
  }
}
