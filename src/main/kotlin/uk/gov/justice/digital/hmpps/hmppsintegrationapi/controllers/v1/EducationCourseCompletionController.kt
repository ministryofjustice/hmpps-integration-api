package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.featureflag.FeatureFlag
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.EducationCourseCompletionRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsMessageResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.EducationCourseCompletionService

@RestController
@Tag(name = "Persons")
class EducationCourseCompletionController(
  @Autowired val educationCourseCompletionService: EducationCourseCompletionService,
) {
  @PostMapping("/v1/education/course-completion")
  @ResponseStatus(HttpStatus.ACCEPTED)
  @Operation(
    summary = "Record a course completion for a person.",
    responses = [
      ApiResponse(responseCode = "202", description = "Course completion accepted for processing."),
      ApiResponse(responseCode = "400", description = "Invalid request data."),
      ApiResponse(responseCode = "500", description = "Internal server error."),
    ],
  )
  @FeatureFlag(name = FeatureFlagConfig.Companion.COURSE_COMPLETION_EVENT)
  fun recordCourseCompletion(
    @RequestBody request: EducationCourseCompletionRequest,
  ): Response<HmppsMessageResponse> = educationCourseCompletionService.recordCourseCompletion(request)
}
