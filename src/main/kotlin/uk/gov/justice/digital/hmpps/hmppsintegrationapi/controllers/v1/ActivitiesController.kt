package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.ValidationException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.featureflag.FeatureFlag
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ActivitySchedule
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DataResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetActivitiesScheduleService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

@RestController
@RequestMapping(value = ["/v1/activities"])
@Tag(name = "Activities")
class ActivitiesController(
  @Autowired val getActivitiesScheduleService: GetActivitiesScheduleService,
  @Autowired val auditService: AuditService,
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
      throw EntityNotFoundException("Could not find prison regime with supplied query parameters.")
    }

    auditService.createEvent(
      "GET_ACTIVITY_SCHEDULES",
      mapOf("activityId" to activityId.toString()),
    )

    return DataResponse(data = response.data)
  }
}
