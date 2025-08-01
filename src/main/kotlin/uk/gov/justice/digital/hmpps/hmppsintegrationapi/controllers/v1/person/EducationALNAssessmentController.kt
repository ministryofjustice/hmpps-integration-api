package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.tags.Tags
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.EducationALNAssessmentsChangeRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsMessageResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.EducationALNAssessmentService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

/**
 * Controller class exposing endpoints relating to a persons' Additional Learning Needs (ALN) Assessments in relation to their in-prison Education.
 */
@RestController
@RequestMapping("/v1/persons/{hmppsId}/education/aln-assessment")
@Tags(value = [Tag(name = "Persons"), Tag(name = "Education")])
class EducationALNAssessmentController(
  private val featureFlag: FeatureFlagConfig,
  private val educationALNAssessmentService: EducationALNAssessmentService,
  private val auditService: AuditService,
) {
  /**
   * API endpoint to notify that a given person/offender has had a change of status to their Education.
   */
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
    summary = "Notify that a person has had a change to their education Additional Needs Assessments.",
    responses = [
      ApiResponse(responseCode = "200", useReturnTypeSchema = true, description = "Successful notification of change of ALN Assessments"),
      ApiResponse(responseCode = "400", content = [Content(schema = Schema(ref = "#/components/schemas/BadRequest"))]),
      ApiResponse(responseCode = "403", content = [Content(schema = Schema(ref = "#/components/schemas/ForbiddenResponse"))]),
      ApiResponse(responseCode = "404", content = [Content(schema = Schema(ref = "#/components/schemas/PersonNotFound"))]),
    ],
  )
  fun notifyChangeOfEducationALNAssessments(
    @Parameter(description = "A HMPPS person identifier", example = "A1234AA") @PathVariable hmppsId: String,
    @Valid @RequestBody request: EducationALNAssessmentsChangeRequest,
  ): Response<HmppsMessageResponse> {
    featureFlag.require(FeatureFlagConfig.EDUCATION_ALN_TRIGGER_ENABLED)

    val response = educationALNAssessmentService.sendEducationALNUpdateEvent(hmppsId, request)
    auditService.createEvent("EDUCATION_ALN_ASSESSMENT_UPDATE_EVENT", mapOf("hmppsId" to hmppsId))
    return response
  }
}
