package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.tags.Tags
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.EducationAssessmentStatusChangeRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsMessageResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.EducationAssessmentService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

/**
 * Controller class exposing endpoints relating to a person's Assessments in relation to their in-prison Education.
 */
@RestController
@RequestMapping("/v1/persons/{hmppsId}/education")
@Tags(value = [Tag(name = "persons"), Tag(name = "education")])
class EducationAssessmentsController(
  private val featureFlag: FeatureFlagConfig,
  private val postAssessmentService: EducationAssessmentService,
  private val auditService: AuditService,
) {
  /**
   * API endpoint to notify that a given person/offender has had a change of status to their Education Assessments.
   */
  @PostMapping("assessments/status")
  @Operation(
    summary = "Notify that a person has had a change in status to their Assessments in relation to their in-prison Education.",
    responses = [
      ApiResponse(responseCode = "200", useReturnTypeSchema = true, description = "Successful notification of change of Education Assessments status"),
      ApiResponse(responseCode = "400", content = [Content(schema = Schema(ref = "#/components/schemas/BadRequest"))]),
      ApiResponse(responseCode = "403", content = [Content(schema = Schema(ref = "#/components/schemas/ForbiddenResponse"))]),
      ApiResponse(responseCode = "404", content = [Content(schema = Schema(ref = "#/components/schemas/PersonNotFound"))]),
    ],
  )
  fun notifyChangeOfEducationAssessmentsStatus(
    @Parameter(description = "A HMPPS person identifier", example = "A1234AA") @PathVariable hmppsId: String,
    @Valid @RequestBody request: EducationAssessmentStatusChangeRequest,
  ): Response<HmppsMessageResponse> {
    featureFlag.require(FeatureFlagConfig.USE_EDUCATION_ASSESSMENTS_ENDPOINTS)

    val response = postAssessmentService.sendEducationAssessmentEvent(hmppsId, request)
    auditService.createEvent("CREATE EDUCATION ASSESSMENT EVENT", mapOf("hmppsId" to hmppsId))
    return response
  }
}
