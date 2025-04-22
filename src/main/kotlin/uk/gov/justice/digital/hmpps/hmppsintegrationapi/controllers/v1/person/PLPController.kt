package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.tags.Tags
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DataResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.InductionSchedule
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.InductionSchedules
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ReviewSchedules
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetInductionScheduleForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetReviewScheduleForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

/**
 * Controller class exposing endpoints that return Learning and work progress (LWP, previously
 * known as PLP) related data about a person.
 */
@RestController
@RequestMapping("/v1/persons")
@Tags(Tag(name = "persons"))
class PLPController(
  private val getInductionScheduleForPersonService: GetInductionScheduleForPersonService,
  private val getReviewScheduleForPersonService: GetReviewScheduleForPersonService,
  private val auditService: AuditService,
) {
  @GetMapping("{hmppsId}/plp-induction-schedule")
  @Operation(
    summary = "Returns the current Learning and work progress (LWP/PLP) Induction Schedule associated with a person.",
    responses = [
      ApiResponse(responseCode = "200", useReturnTypeSchema = true, description = "Successfully found Induction Schedule for a person with the provided HMPPS ID."),
      ApiResponse(responseCode = "404", content = [Content(schema = Schema(ref = "#/components/schemas/PersonNotFound"))]),
      ApiResponse(responseCode = "500", content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))]),
    ],
  )
  fun getInductionSchedule(
    @Parameter(description = "A HMPPS id", example = "A123123") @PathVariable hmppsId: String,
  ): DataResponse<InductionSchedule> {
    val response = getInductionScheduleForPersonService.execute(hmppsId)

    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not find person with id: $hmppsId")
    }
    auditService.createEvent("GET_INDUCTION_SCHEDULE", mapOf("hmppsId" to hmppsId))
    return DataResponse(response.data)
  }

  @GetMapping("{hmppsId}/plp-induction-schedule/history")
  @Operation(
    summary = "Returns the history of changes to the Learning and work progress (LWP/PLP) Induction Schedule associated with a person.",
    responses = [
      ApiResponse(responseCode = "200", useReturnTypeSchema = true, description = "Successfully found Induction Schedule history for a person with the provided HMPPS ID."),
      ApiResponse(responseCode = "404", content = [Content(schema = Schema(ref = "#/components/schemas/PersonNotFound"))]),
      ApiResponse(responseCode = "500", content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))]),
    ],
  )
  fun getInductionScheduleHistory(
    @Parameter(description = "A HMPPS id", example = "A123123") @PathVariable hmppsId: String,
  ): DataResponse<InductionSchedules> {
    val response = getInductionScheduleForPersonService.getHistory(hmppsId)

    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not find person with id: $hmppsId")
    }
    auditService.createEvent("GET_INDUCTION_SCHEDULE", mapOf("hmppsId" to hmppsId))
    return DataResponse(response.data)
  }

  @GetMapping("{hmppsId}/plp-review-schedule")
  @Operation(
    summary = "Returns the history of Learning and work progress (LWP/PLP) Review Schedules associated with a person.",
    responses = [
      ApiResponse(responseCode = "200", useReturnTypeSchema = true, description = "Successfully found Review Schedule history for a person with the provided HMPPS ID."),
      ApiResponse(responseCode = "404", content = [Content(schema = Schema(ref = "#/components/schemas/PersonNotFound"))]),
      ApiResponse(responseCode = "500", content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))]),
    ],
  )
  fun getReviewScheduleHistory(
    @Parameter(description = "A HmppsId", example = "A123123") @PathVariable hmppsId: String,
    @Parameter(description = "Filter by review schedule statuses", example = "[\"COMPLETED\", \"PENDING\"]")
    @RequestParam(required = false) statuses: List<String>?,
  ): DataResponse<ReviewSchedules> {
    val response = getReviewScheduleForPersonService.execute(hmppsId)

    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not find person with id: $hmppsId")
    }

    auditService.createEvent("GET_REVIEW_SCHEDULE_HISTORY", mapOf("hmppsId" to hmppsId))

    // Filter the review schedules by statuses if the query parameter is provided
    val filteredData =
      response.data.reviewSchedules
        .filter { statuses == null || it.status in statuses }

    return DataResponse(ReviewSchedules(filteredData))
  }
}
