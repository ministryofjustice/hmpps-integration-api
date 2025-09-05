package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.prison

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.tags.Tags
import jakarta.validation.Valid
import jakarta.validation.ValidationException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.ConflictFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DataResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DeactivateLocationRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsMessageResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Location
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError.Type.BAD_REQUEST
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError.Type.ENTITY_NOT_FOUND
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetLocationByKeyService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.LocationQueueService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

@RestController
@RequestMapping("/v1/prison/{prisonId}/location")
@Tags(value = [Tag("Prison"), Tag("Residential Areas")])
class LocationController(
  @Autowired val auditService: AuditService,
  @Autowired val getLocationByKeyService: GetLocationByKeyService,
  @Autowired val locationQueueService: LocationQueueService,
) {
  @GetMapping("/{key}")
  @Operation(
    summary = "Gets the location information for a prison location based on a key.",
    description = "<b>Applicable filters</b>: <ul><li>prisons</li></ul>",
    responses = [
      ApiResponse(responseCode = "200", useReturnTypeSchema = true, description = "Successfully performed the query on upstream APIs."),
      ApiResponse(
        responseCode = "400",
        description = "",
        content = [Content(schema = Schema(ref = "#/components/schemas/BadRequest"))],
      ),
      ApiResponse(responseCode = "403", content = [Content(schema = Schema(ref = "#/components/schemas/ForbiddenResponse"))]),
      ApiResponse(responseCode = "404", content = [Content(schema = Schema(ref = "#/components/schemas/PersonNotFound"))]),
      ApiResponse(responseCode = "500", content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))]),
    ],
  )
  fun getLocationInformation(
    @Parameter(description = "The ID of the prison to be queried against") @PathVariable prisonId: String,
    @Parameter(description = "The key of the location to be queried against in the format of (PrisonId-locationKey") @PathVariable key: String,
    @RequestAttribute filters: RoleFilters?,
  ): DataResponse<Location?> {
    val response = getLocationByKeyService.execute(prisonId, key, filters)

    if (response.hasError(UpstreamApiError.Type.BAD_REQUEST)) {
      throw ValidationException("Invalid query parameters.")
    }

    if (response.hasError(ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not find location information with supplied query parameters.")
    }

    auditService.createEvent(
      "GET_LOCATION_INFORMATION",
      mapOf("prisonId" to prisonId, "key" to key),
    )

    return DataResponse(data = response.data)
  }

  @PostMapping("/{key}/deactivate")
  @Operation(
    summary = "Temporarily mark a location as inactive. The location must be a cell.",
    description = "<b>Applicable filters</b>: <ul><li>prisons</li></ul>",
    responses = [
      ApiResponse(responseCode = "200", useReturnTypeSchema = true, description = "Successfully posted to upstream APIs."),
      ApiResponse(
        responseCode = "400",
        description = "",
        content = [Content(schema = Schema(ref = "#/components/schemas/BadRequest"))],
      ),
      ApiResponse(responseCode = "403", content = [Content(schema = Schema(ref = "#/components/schemas/ForbiddenResponse"))]),
      ApiResponse(responseCode = "404", content = [Content(schema = Schema(ref = "#/components/schemas/PersonNotFound"))]),
      ApiResponse(responseCode = "500", content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))]),
    ],
  )
  fun deactivateLocation(
    @Parameter(description = "The ID of the prison the location is in") @PathVariable prisonId: String,
    @Parameter(description = "The key of the location, must be a cell") @PathVariable key: String,
    @Valid @RequestBody deactivateLocationRequest: DeactivateLocationRequest,
    @RequestAttribute clientName: String?,
    @RequestAttribute filters: RoleFilters?,
  ): DataResponse<HmppsMessageResponse?> {
    val response = locationQueueService.sendDeactivateLocationRequest(deactivateLocationRequest, prisonId, key, clientName.orEmpty(), filters)
    if (response.hasError(ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException(response.errors[0].description ?: "Could not find provided location.")
    }

    if (response.hasError(BAD_REQUEST)) {
      throw ValidationException(response.errors[0].description ?: "Invalid request.")
    }

    if (response.hasError(UpstreamApiError.Type.CONFLICT)) {
      throw ConflictFoundException(response.errors[0].description ?: "Conflict.")
    }

    auditService.createEvent("DEACTIVATE_LOCATION", mapOf("prisonId" to prisonId, "key" to key))

    return DataResponse(response.data)
  }
}
