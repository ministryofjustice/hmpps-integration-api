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
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.ConflictFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.ForbiddenByUpstreamServiceException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.featureflag.FeatureFlag
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ActivitySchedule
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ActivityScheduleDetailed
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.AttendanceUpdateRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DataResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DeallocationReason
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsMessageResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonerAllocationRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonerDeallocationRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ReasonForAttendance
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.SuitabilityCriteria
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.WaitingListApplication
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.ActivitiesQueueService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetActivitiesScheduleService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetActivitiesSuitabilityCriteriaService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetAttendanceReasonsService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetDeallocationReasonsService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetScheduleDetailsService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetWaitingListApplicationsByScheduleIdService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

@RestController
@RequestMapping(value = ["/v1/activities"])
@Tag(name = "Activities")
class ActivitiesController(
  @Autowired val getActivitiesScheduleService: GetActivitiesScheduleService,
  @Autowired val getAttendanceReasonsService: GetAttendanceReasonsService,
  @Autowired val getDeallocationReasonsService: GetDeallocationReasonsService,
  @Autowired val getWaitingListApplicationsByScheduleIdService: GetWaitingListApplicationsByScheduleIdService,
  @Autowired val auditService: AuditService,
  private val activitiesQueueService: ActivitiesQueueService,
  private val getScheduleDetailsService: GetScheduleDetailsService,
  private val getActivitiesSuitabilityCriteriaService: GetActivitiesSuitabilityCriteriaService,
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

  @GetMapping("/schedule/{scheduleId}/suitability-criteria")
  @Operation(
    summary = "Gets the suitability criteria for allocating prisoners to a particular activity schedule",
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
  @FeatureFlag(name = FeatureFlagConfig.USE_SUITABILITY_ENDPOINT)
  fun getActivityScheduleSuitabilityCriteria(
    @Parameter(description = "The ID of the schedule") @PathVariable scheduleId: Long,
    @RequestAttribute filters: ConsumerFilters?,
  ): DataResponse<SuitabilityCriteria?> {
    val response = getActivitiesSuitabilityCriteriaService.execute(scheduleId, filters)

    if (response.hasError(UpstreamApiError.Type.BAD_REQUEST)) {
      throw ValidationException("Invalid query parameters.")
    }

    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not find suitability criteria for supplied schedule id: $scheduleId.")
    }

    auditService.createEvent(
      "GET_ACTIVITY_SCHEDULE_SUITABILITY_CRITERIA",
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
  @Tag(name = "Reference Data")
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

  @PutMapping("/schedule/{scheduleId}/deallocate")
  @Operation(
    summary = "Deallocate prisoner from activity.",
    description = "<b>Applicable filters</b>: <ul><li>prisons</li></ul>",
    responses = [
      ApiResponse(
        responseCode = "200",
        useReturnTypeSchema = true,
        description = "Prisoner deallocation written to queue.",
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
  @FeatureFlag(name = FeatureFlagConfig.Companion.USE_DEALLOCATION_ENDPOINT)
  fun putDeallocateFromSchedule(
    @Parameter(description = "The ID of the schedule") @PathVariable scheduleId: Long,
    @RequestAttribute filters: ConsumerFilters?,
    @RequestAttribute clientName: String,
    @RequestBody @Valid prisonerDeallocationRequest: PrisonerDeallocationRequest,
  ): DataResponse<HmppsMessageResponse?> {
    val response = activitiesQueueService.sendPrisonerDeallocationRequest(scheduleId, prisonerDeallocationRequest, clientName, filters)

    if (response.hasError(UpstreamApiError.Type.BAD_REQUEST)) {
      throw ValidationException("Bad request: ${response.errors[0].description}")
    }

    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Not found: ${response.errors[0].description}")
    }

    auditService.createEvent(
      "PUT_DEALLOCATE_PRISONER_FROM_ACTIVITY",
      mapOf("scheduleId" to scheduleId.toString()),
    )

    return DataResponse(data = response.data)
  }

  @FeatureFlag(name = FeatureFlagConfig.Companion.USE_DEALLOCATION_REASONS_ENDPOINT)
  @Tag(name = "Reference Data")
  @GetMapping("/deallocation-reasons")
  @Operation(
    summary = "Gets possible reasons for deallocation.",
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
  fun getDeallocationReasons(): DataResponse<List<DeallocationReason>?> {
    val response = getDeallocationReasonsService.execute()

    if (response.hasError(UpstreamApiError.Type.FORBIDDEN)) {
      throw ForbiddenByUpstreamServiceException("Access denied to deallocation reasons.")
    }

    auditService.createEvent(
      "GET_DEALLOCATION_REASONS",
      mapOf(),
    )

    return DataResponse(data = response.data)
  }

  @PostMapping("/schedule/{scheduleId}/allocate")
  @Operation(
    summary = "Allocate prisoner to activity.",
    description = "<b>Applicable filters</b>: <ul><li>prisons</li></ul>",
    responses = [
      ApiResponse(
        responseCode = "200",
        useReturnTypeSchema = true,
        description = "Prisoner allocation written to queue.",
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
        responseCode = "409",
        content = [Content(schema = Schema(ref = "#/components/schemas/ConflictResponse"))],
      ),
      ApiResponse(
        responseCode = "500",
        content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))],
      ),
    ],
  )
  @FeatureFlag(name = FeatureFlagConfig.Companion.USE_ALLOCATION_ENDPOINT)
  fun postAllocationEndpoint(
    @Parameter(description = "The ID of the schedule") @PathVariable scheduleId: Long,
    @RequestAttribute filters: ConsumerFilters?,
    @RequestAttribute clientName: String,
    @RequestBody @Valid prisonerAllocationRequest: PrisonerAllocationRequest,
  ): DataResponse<HmppsMessageResponse?> {
    val response = activitiesQueueService.sendPrisonerAllocationRequest(scheduleId, prisonerAllocationRequest, clientName, filters)

    if (response.hasError(UpstreamApiError.Type.BAD_REQUEST)) {
      throw ValidationException("Invalid query parameters: ${response.errors[0].description}")
    }

    if (response.hasError(UpstreamApiError.Type.CONFLICT)) {
      throw ConflictFoundException("Conflict: ${response.errors[0].description}")
    }

    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not find records for supplied requests: ${response.errors[0].description}")
    }

    auditService.createEvent(
      "POST_ALLOCATE_PRISONER_TO_ACTIVITY",
      mapOf("scheduleId" to scheduleId.toString()),
    )

    return DataResponse(data = response.data)
  }

  @GetMapping("/schedule/{scheduleId}/waiting-list-applications")
  @Operation(
    summary = "Gets the waiting list applications for an activity.",
    description = "<b>Applicable filters</b>: <ul><li>prisons</li></ul>",
    responses = [
      ApiResponse(
        responseCode = "200",
        useReturnTypeSchema = true,
        description = "Successfully performed the query on upstream APIs. An empty list is returned when no results are found.",
      ),
      ApiResponse(
        responseCode = "400",
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
  @FeatureFlag(name = FeatureFlagConfig.Companion.USE_WAITING_LIST_ENDPOINT)
  fun getWaitingListApplicationsByScheduleId(
    @Parameter(description = "The ID of the schedule") @PathVariable scheduleId: Long,
    @RequestAttribute filters: ConsumerFilters?,
  ): DataResponse<List<WaitingListApplication>?> {
    val response = getWaitingListApplicationsByScheduleIdService.execute(scheduleId, filters)

    if (response.hasError(UpstreamApiError.Type.BAD_REQUEST)) {
      throw ValidationException("Invalid query parameters.")
    }

    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not find waiting list applications with supplied query parameters.")
    }

    auditService.createEvent(
      "GET_WAITING_LIST_APPLICATIONS_BY_ID",
      mapOf("scheduleId" to scheduleId.toString()),
    )

    return DataResponse(data = response.data)
  }
}
