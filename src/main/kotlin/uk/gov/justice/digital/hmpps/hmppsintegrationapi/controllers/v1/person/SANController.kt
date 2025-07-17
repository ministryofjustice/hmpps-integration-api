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
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.featureflag.FeatureFlag
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DataResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PlanCreationSchedules
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetSANPLanScheduleForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

/**
* Controller class exposing endpoints that return Support for additional needs (SAN) related data about a person.
*/
@RestController
@RequestMapping("/v1/persons")
@Tags(value = [Tag(name = "Persons"), Tag(name = "SAN")])
class SANController(
  private val getSANPLanScheduleForPersonService: GetSANPLanScheduleForPersonService,
  private val auditService: AuditService,
) {
  @FeatureFlag(name = FeatureFlagConfig.USE_SAN_ENDPOINT)
  @GetMapping("{hmppsId}/education/san/plan-creation-schedule")
  @Operation(
    summary = "Returns the history of changes to the Support Additional Needs (SAN) Plan Creation Schedule associated with a person.",
    responses = [
      ApiResponse(responseCode = "200", useReturnTypeSchema = true, description = "Successfully found Plan Creation Schedule history for a person with the provided HMPPS ID."),
      ApiResponse(responseCode = "404", content = [Content(schema = Schema(ref = "#/components/schemas/PersonNotFound"))]),
      ApiResponse(responseCode = "500", content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))]),
    ],
  )
  fun getPlanCreationSchedule(
    @Parameter(description = "A HMPPS id", example = "A123123") @PathVariable hmppsId: String,
  ): DataResponse<PlanCreationSchedules> {
    val response = getSANPLanScheduleForPersonService.getPlanCreationSchedules(hmppsId)

    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not find person with id: $hmppsId")
    }
    auditService.createEvent("GET_SAN_PLAN_CREATION_SCHEDULE", mapOf("hmppsId" to hmppsId))
    return DataResponse(response.data)
  }
}
