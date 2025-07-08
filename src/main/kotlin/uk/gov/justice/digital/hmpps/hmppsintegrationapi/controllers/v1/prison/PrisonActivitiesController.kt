package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.prison

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
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.featureflag.FeatureFlag
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ActivityScheduledInstanceForPrisoner
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.AppointmentDetails
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.AppointmentSearchRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DataResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.RunningActivity
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError.Type.BAD_REQUEST
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError.Type.ENTITY_NOT_FOUND
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPrisonActivitiesService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetScheduledInstancesForPrisonerService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.SearchAppointmentsService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

@RestController
@RequestMapping(value = ["/v1/prison"])
@Tag(name = "Activities")
class PrisonActivitiesController(
  @Autowired val getPrisonActivitiesService: GetPrisonActivitiesService,
  @Autowired val getScheduledInstancesForPrisonerService: GetScheduledInstancesForPrisonerService,
  @Autowired val auditService: AuditService,
  private val searchAppointmentsService: SearchAppointmentsService,
) {
  @GetMapping("/{prisonId}/activities")
  @Operation(
    summary = "Returns all running prison activities given a prisonId",
    description = "<b>Applicable filters</b>: <ul><li>prisons</li></ul>",
    responses = [
      ApiResponse(responseCode = "200", useReturnTypeSchema = true, description = "Successfully found a prison with the provided prison ID."),
      ApiResponse(responseCode = "403", content = [Content(schema = Schema(ref = "#/components/schemas/ForbiddenResponse"))]),
      ApiResponse(responseCode = "404", content = [Content(schema = Schema(ref = "#/components/schemas/PrisonNotFound"))]),
      ApiResponse(responseCode = "500", content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))]),
    ],
  )
  fun getPrisonActivities(
    @Parameter(description = "The ID of the prison to be queried against") @PathVariable prisonId: String,
    @RequestAttribute filters: ConsumerFilters?,
  ): DataResponse<List<RunningActivity>?> {
    val response = getPrisonActivitiesService.execute(prisonId, filters)

    if (response.hasError(BAD_REQUEST)) {
      throw ValidationException("Invalid query parameters.")
    }

    if (response.hasError(ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not find prison regime with supplied query parameters.")
    }

    auditService.createEvent(
      "GET_PRISON_ACTIVITIES",
      mapOf("prisonId" to prisonId),
    )

    return DataResponse(data = response.data)
  }

  @FeatureFlag(name = FeatureFlagConfig.Companion.USE_SCHEDULED_INSTANCES_ENDPOINT)
  @GetMapping("/{prisonId}/prisoners/{hmppsId}/scheduled-instances")
  @Operation(
    summary = "Returns all scheduled instances for a prisoner given a prisonId and hmppsId.",
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
  fun getScheduledInstancesForPrisoner(
    @Parameter(description = "The ID of the prison to be queried against") @PathVariable prisonId: String,
    @Parameter(description = "The ID of the prisoner to be queried against") @PathVariable hmppsId: String,
    @Parameter(description = "The start date of the search range (YYYY-MM-DD)", required = true)
    @RequestParam startDate: String,
    @Parameter(description = "The end date of the search range (YYYY-MM-DD)", required = true)
    @RequestParam endDate: String,
    @Parameter(description = "Optional time slot filter", required = false)
    @RequestParam(required = false) slot: String? = null,
    @RequestAttribute filters: ConsumerFilters?,
  ): DataResponse<List<ActivityScheduledInstanceForPrisoner>?> {
    val response = getScheduledInstancesForPrisonerService.execute(prisonId, hmppsId, startDate, endDate, slot, filters)

    if (response.hasError(BAD_REQUEST)) {
      throw ValidationException("Invalid query parameters.")
    }

    if (response.hasError(ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not find scheduled instances with supplied query parameters.")
    }

    auditService.createEvent(
      "GET_SCHEDULED_INSTANCES_FOR_PRISONER",
      mapOf("prisonId" to prisonId, "hmppsId" to hmppsId, "startDate" to startDate, "endDate" to endDate, "slot" to slot),
    )

    return DataResponse(data = response.data)
  }

  @PostMapping("/{prisonId}/appointments/search")
  @Operation(
    summary = "Returns all appointments within a specified prison, which match the request body parameters.",
    description = "<b>Applicable filters</b>: <ul><li>prisons</li></ul>",
    responses = [
      ApiResponse(responseCode = "200", useReturnTypeSchema = true, description = "Successfully found data from parameters provided."),
      ApiResponse(responseCode = "400", content = [Content(schema = Schema(ref = "#/components/schemas/BadRequest"))]),
      ApiResponse(responseCode = "403", content = [Content(schema = Schema(ref = "#/components/schemas/ForbiddenResponse"))]),
      ApiResponse(responseCode = "404", content = [Content(schema = Schema(ref = "#/components/schemas/PrisonNotFound"))]),
      ApiResponse(responseCode = "500", content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))]),
    ],
  )
  @FeatureFlag(name = FeatureFlagConfig.USE_SEARCH_APPOINTMENTS_ENDPOINT)
  fun searchAppointments(
    @Parameter(description = "The ID of the prison to be queried against") @PathVariable prisonId: String,
    @RequestAttribute filters: ConsumerFilters?,
    @Valid @RequestBody request: AppointmentSearchRequest,
  ): DataResponse<List<AppointmentDetails>?> {
    val response = searchAppointmentsService.execute(prisonId, request, filters)

    if (response.hasError(BAD_REQUEST)) {
      throw ValidationException("Invalid query parameters.")
    }

    if (response.hasError(ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not find appointments with supplied query parameters.")
    }

    auditService.createEvent(
      "SEARCH_APPOINTMENTS",
      mapOf("prisonId" to prisonId, "startDate" to request.startDate.toString()),
    )

    return DataResponse(data = response.data)
  }
}
