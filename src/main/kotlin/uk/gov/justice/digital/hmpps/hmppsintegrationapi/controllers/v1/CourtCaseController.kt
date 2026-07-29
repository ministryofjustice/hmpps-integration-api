package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.ValidationException
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CourtCasesSummary
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DataResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.CourtCaseService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

@RestController
@RequestMapping("/v1/persons/{hmppsId}/court-cases")
@Tag(name = "Court Cases")
class CourtCaseController(
  private val auditService: AuditService,
  private val courtCaseService: CourtCaseService,
) {
  @GetMapping
  @Operation(
    summary = "Returns a summary of an offenders court case history.",
    description =
      "Returns information regarding an individuals court case history.<br /> <br />" +
        "This returns: <br />" +
        "- The date of their first conviction <br />" +
        "- The court code of their latest sentenced outcome <br />" +
        "- The outcome of their last court appearance including outcome name and outcome code. e.g Imprisonment (SENTENCING), Appeal (APPEAL) or Remand (Remand) <br /><br />" +
        "The following outcome type codes are supported: <br />" +
        "- SENTENCING </br>" +
        "- APPEAL </br>" +
        "- REMAND </br>" +
        "- OTHER  </br>",
    responses = [
      ApiResponse(responseCode = "200", useReturnTypeSchema = true, description = "Successfully found court cases summary."),
      ApiResponse(responseCode = "404", content = [Content(schema = Schema(ref = "#/components/schemas/PersonNotFound"))]),
      ApiResponse(responseCode = "500", content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))]),
    ],
  )
  fun getCourtCases(
    @Parameter(description = "HMPPS identifier", example = "A1234AA") @PathVariable hmppsId: String,
    @RequestAttribute requestContext: RequestContext?,
  ): DataResponse<CourtCasesSummary?> {
    val response = courtCaseService.getCourtCaseDetails(hmppsId, requestContext)

    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException(hmppsId)
    }

    if (response.hasError(UpstreamApiError.Type.BAD_REQUEST)) {
      throw ValidationException("Invalid hmppsId.")
    }

    auditService.createEvent("COURT_CASES_SUMMARY", mapOf("hmppsId" to hmppsId))
    return DataResponse(response.data)
  }
}
