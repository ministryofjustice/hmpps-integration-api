package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.featureflag.FeatureFlag
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.up3.CaseStatusUpdate
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.ReceiveCaseStatusService

@RestController
@RequestMapping("/v1/cases")
@Tag(name = "Cases")
class CaseStatusController(
  private val receiveCaseStatusService: ReceiveCaseStatusService,
) {
  @PutMapping("/{caseId}/status")
  @ResponseStatus(HttpStatus.OK)
  @Operation(
    summary = "Receive a case status update from UP3",
    description = "Validates the case status update against CEMO and publishes it to the EM notification SNS topic before responding.",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Case status update received successfully. No response body is returned.",
      ),
      ApiResponse(
        responseCode = "400",
        description = "Malformed request or missing required data.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(responseCode = "401", description = "Unauthorized."),
      ApiResponse(responseCode = "403", description = "Caller is not authorized to use this endpoint."),
      ApiResponse(
        responseCode = "404",
        description = "No CEMO order was found for the case ID.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "422",
        description = "The status update violates a contract rule.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "500",
        description = "Unexpected server error.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  @FeatureFlag(name = FeatureFlagConfig.UP3_CASE_STATUS_UPDATE_ENABLED)
  fun updateCaseStatus(
    @PathVariable caseId: String,
    @Valid @RequestBody request: CaseStatusUpdate,
  ) {
    receiveCaseStatusService.receive(caseId, request)
  }
}
