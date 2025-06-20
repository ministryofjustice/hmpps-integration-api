package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.ValidationException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.ForbiddenByUpstreamServiceException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.featureflag.FeatureFlag
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ActivitySchedule
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ActivityScheduleDetailed
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.AttendanceUpdateRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DataResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsMessageResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ReasonForAttendance
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.ActivitiesQueueService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetActivitiesScheduleService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetAttendanceReasonsService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetScheduleDetailsService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

@RestController
@RequestMapping(value = ["/v1/activities"])
@Tag(name = "Activities")
class ActivitiesController(
  @Autowired val getActivitiesScheduleService: GetActivitiesScheduleService,
  @Autowired val getAttendanceReasonsService: GetAttendanceReasonsService,
  @Autowired val auditService: AuditService,
  private val activitiesQueueService: ActivitiesQueueService,
  private val getScheduleDetailsService: GetScheduleDetailsService,
) {
  @GetMapping("/{activityId}/schedules")
  @Operation(
    summary = "Gets the schedule for an activity.",
    description = "<b>Applicable filters</b>: <ul><li>prisons</li></ul>",
    responses = [
      ApiResponse(
        responseCode = "200",
        useReturnTypeSchema = true,
        description = "Successfully performed the query on upstream APIs. An empty list is returned when no results are found.",
      ),
      ApiResponse(
        responseCode = "403",
        content = [Content(schema = Schema(ref = "#/components/schemas/ForbiddenResponse"))],
      ),
      ApiResponse(
        responseCode = "404",
        content = [Content(schema = Schema(ref = "#/components/schemas/NotFoundError"))],
      ),
      ApiResponse(
        responseCode = "500",
        content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))],
      ),
    ],
  )
  @FeatureFlag(name = FeatureFlagConfig.Companion.USE_ACTIVITIES_SCHEDULE_ENDPOINT)
  fun getActivitySchedules(
    @Parameter(description = "The ID of the activity") @PathVariable activityId: Long,
    @RequestAttribute filters: ConsumerFilters?,
  ): DataResponse<List<ActivitySchedule>?> {
    val response = getActivitiesScheduleService.execute(activityId, filters)

    if (response.hasError(UpstreamApiError.Type.BAD_REQUEST)) {
      throw ValidationException("Invalid query parameters.")
    }

    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not find activity schedule with supplied query parameters.")
    }

    auditService.createEvent(
      "GET_ACTIVITY_SCHEDULES",
      mapOf("activityId" to activityId.toString()),
    )

    return DataResponse(data = response.data)
  }

  @GetMapping("/schedule/{scheduleId}")
  @Operation(
    summary = "Gets the schedule details for an activity.",
    description = "<b>Applicable filters</b>: <ul><li>prisons</li></ul>",
    responses = [
      ApiResponse(
        responseCode = "200",
        useReturnTypeSchema = true,
        description = "Successfully performed the query on upstream APIs.",
      ),
      ApiResponse(
        responseCode = "400",
        description = "Invalid query parameters.",
        content = [Content(schema = Schema(ref = "#/components/schemas/BadRequest"))],
      ),
      ApiResponse(
        responseCode = "403",
        content = [Content(schema = Schema(ref = "#/components/schemas/ForbiddenResponse"))],
      ),
      ApiResponse(
        responseCode = "404",
        content = [Content(schema = Schema(ref = "#/components/schemas/NotFoundError"))],
      ),
      ApiResponse(
        responseCode = "500",
        content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))],
      ),
    ],
  )
  @FeatureFlag(name = FeatureFlagConfig.USE_SCHEDULE_DETAIL_ENDPOINT)
  fun getScheduleDetails(
    @Parameter(description = "The ID of the schedule") @PathVariable scheduleId: Long,
    @RequestAttribute filters: ConsumerFilters?,
  ): DataResponse<ActivityScheduleDetailed?> {
    val response = getScheduleDetailsService.execute(scheduleId, filters)

    if (response.hasError(UpstreamApiError.Type.BAD_REQUEST)) {
      throw ValidationException("Invalid query parameters.")
    }

    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not find schedule details for supplied schedule id: $scheduleId.")
    }

    auditService.createEvent(
      "GET_SCHEDULE_DETAILS",
      mapOf("scheduleId" to scheduleId.toString()),
    )

    return DataResponse(data = response.data)
  }

  @PutMapping("/schedule/attendance")
  @Operation(
    summary = "Mark prisoner attendances. The attendances are created at the start of the day for the day’s activities. Get the attendance IDs by calling the get schedule by its ID.",
    description = "<b>Applicable filters</b>: <ul><li>prisons</li></ul>",
    responses = [
      ApiResponse(
        responseCode = "200",
        useReturnTypeSchema = true,
        description = "Successfully performed the query on upstream APIs. An empty list is returned when no results are found.",
      ),
      ApiResponse(
        responseCode = "400",
        description = "The request body provided has a field in an invalid format.",
        content = [Content(schema = Schema(ref = "#/components/schemas/BadRequest"))],
      ),
      ApiResponse(
        responseCode = "403",
        content = [Content(schema = Schema(ref = "#/components/schemas/ForbiddenResponse"))],
      ),
      ApiResponse(
        responseCode = "404",
        content = [Content(schema = Schema(ref = "#/components/schemas/NotFoundError"))],
      ),
      ApiResponse(
        responseCode = "500",
        content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))],
      ),
    ],
  )
  @FeatureFlag(name = FeatureFlagConfig.Companion.USE_UPDATE_ATTENDANCE_ENDPOINT)
  fun putAttendance(
    @RequestAttribute filters: ConsumerFilters?,
    @RequestAttribute clientName: String,
    @RequestBody @Valid attendanceUpdateRequests: List<AttendanceUpdateRequest>,
  ): DataResponse<HmppsMessageResponse?> {
    val response = activitiesQueueService.sendAttendanceUpdateRequest(attendanceUpdateRequests, clientName, filters)

    if (response.hasError(UpstreamApiError.Type.BAD_REQUEST)) {
      throw ValidationException("Invalid query parameters.")
    }

    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not find attendance records for supplied requests.")
    }

    auditService.createEvent(
      "PUT_ATTENDANCE",
      mapOf("attendanceIds" to attendanceUpdateRequests.joinToString(", ") { it.id.toString() }),
    )

    return DataResponse(data = response.data)
  }

  @GetMapping("/attendance-reasons")
  @Operation(
    summary = "Gets possible reasons for attendance.",
    description = "<b>Applicable filters</b>: <ul><li>prisons</li></ul>",
    responses = [
      ApiResponse(
        responseCode = "200",
        useReturnTypeSchema = true,
        description = "Successfully performed the query on upstream APIs. An empty list is returned when no results are found.",
      ),
      ApiResponse(
        responseCode = "403",
        content = [Content(schema = Schema(ref = "#/components/schemas/ForbiddenResponse"))],
      ),
      ApiResponse(
        responseCode = "500",
        content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))],
      ),
    ],
  )
  @FeatureFlag(name = FeatureFlagConfig.Companion.USE_ATTENDANCE_REASONS_ENDPOINT)
  fun getAttendanceReasons(): DataResponse<List<ReasonForAttendance>?> {
    val response = getAttendanceReasonsService.execute()

    if (response.hasError(UpstreamApiError.Type.FORBIDDEN)) {
      throw ForbiddenByUpstreamServiceException("Access denied to attendance reasons.")
    }

    auditService.createEvent(
      "GET_ATTENDANCE_REASONS",
      mapOf(),
    )

    return DataResponse(data = response.data)
  }
}
