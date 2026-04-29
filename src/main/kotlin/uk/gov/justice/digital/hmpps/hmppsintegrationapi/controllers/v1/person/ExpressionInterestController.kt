package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.DeprecatedApiException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response

@RestController
@RequestMapping("/v1/persons")
@Tag(name = "Persons")
class ExpressionInterestController {
  /**
   * API endpoint to notify that a given person/offender has expressed an interest in a job.
   */
  @Deprecated(message = "This API has been deprecated.", level = DeprecationLevel.ERROR)
  @PutMapping("{hmppsId}/expression-of-interest/jobs/{jobId}")
  @Operation(
    deprecated = true,
    summary = "Notify that a person has expressed an interest in a particular job.",
    responses = [
      ApiResponse(responseCode = "403", content = [Content(schema = Schema(ref = "#/components/schemas/ForbiddenResponse"))]),
      ApiResponse(responseCode = "410", content = [Content(schema = Schema(ref = "#/components/schemas/DeprecatedApiError"))]),
    ],
  )
  fun submitExpressionOfInterest(
    @Parameter(description = "A HMPPS identifier", example = "A1234AA") @PathVariable hmppsId: String,
    @Parameter(description = "A job identifier") @PathVariable jobId: String,
  ): Response<Unit> = throw DeprecatedApiException()
}
