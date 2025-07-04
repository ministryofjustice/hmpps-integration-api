package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.tags.Tags
import jakarta.validation.ValidationException
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DataResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonerBaseLocation
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPrisonerBaseLocationForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

@RestController
@RequestMapping("/v1/persons")
@Tags(value = [Tag(name = "Persons")])
class PrisonerBaseLocationController(
  private val featureFlag: FeatureFlagConfig,
  private val getPrisonerBaseLocationForPersonService: GetPrisonerBaseLocationForPersonService,
  private val auditService: AuditService,
) {
  @GetMapping("{hmppsId}/prisoner-base-location")
  @Operation(
    summary = "Returns prisoner's base location of a person",
    responses = [
      ApiResponse(responseCode = "200", useReturnTypeSchema = true, description = "Successfully found prisoner's base location."),
      ApiResponse(responseCode = "404", content = [Content(schema = Schema(ref = "#/components/schemas/PersonNotFound"))]),
      ApiResponse(responseCode = "400", content = [Content(schema = Schema(ref = "#/components/schemas/BadRequest"))]),
    ],
  )
  fun getPrisonerBaseLocation(
    @Parameter(description = "A HMPPS id", example = "A123123") @PathVariable hmppsId: String,
    @RequestAttribute filters: ConsumerFilters?,
  ): DataResponse<PrisonerBaseLocation?> {
    featureFlag.require(FeatureFlagConfig.USE_PRISONER_BASE_LOCATION_ENDPOINT)

    val response = getPrisonerBaseLocationForPersonService.execute(hmppsId, filters)
    when {
      response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND) -> throw EntityNotFoundException("Could not find prisoner base location for id: $hmppsId")
      response.hasError(UpstreamApiError.Type.BAD_REQUEST) -> throw ValidationException("Invalid HMPPS ID: $hmppsId")
    }

    auditService.createEvent("GET_PERSON_PRISONER_BASE_LOCATION", mapOf("hmppsId" to hmppsId))

    return DataResponse(data = response.data)
  }
}
