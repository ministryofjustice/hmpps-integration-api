package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

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
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.featureflag.FeatureFlag
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ContactEvent
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DataResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.interfaces.toPaginatedResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.ContactEventService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.PaginatedResponse

@RestController
@RequestMapping("/v1/persons/{hmppsId}/contact-events")
@Tag(name = "Persons")
class ContactEventsController(
  @Autowired val auditService: AuditService,
  @Autowired val featureFlag: FeatureFlagConfig,
  @Autowired val contactEventService: ContactEventService,
) {
  @GetMapping
  @Tag(name = "Contact Events")
  @Operation(
    summary = "Returns a (potentially empty) list of ContactEvent objects for a person.",
    responses = [
      ApiResponse(
        responseCode = "200",
        useReturnTypeSchema = true,
        description = "Successfully found a person's contact events with the provided HMPPS ID.",
      ),
      ApiResponse(
        responseCode = "400",
        content = [Content(schema = Schema(ref = "#/components/schemas/BadRequest"))],
      ),
      ApiResponse(
        responseCode = "404",
        content = [Content(schema = Schema(ref = "#/components/schemas/PersonNotFound"))],
      ),
      ApiResponse(
        responseCode = "500",
        content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))],
      ),
    ],
  )
  @FeatureFlag(name = FeatureFlagConfig.USE_CONTACT_EVENTS_ENDPOINT)
  fun getContactEvents(
    @Parameter(description = "A HMPPS identifier") @PathVariable hmppsId: String,
    @Parameter(description = "The page number (starting from 1)", schema = Schema(minimum = "1")) @RequestParam(required = false, defaultValue = "1", name = "page") page: Int,
    @Parameter(description = "The maximum number of results for a page", schema = Schema(minimum = "1")) @RequestParam(required = false, defaultValue = "10", name = "perPage") perPage: Int,
  ): PaginatedResponse<ContactEvent> {
    val response = contactEventService.getContactEvents(hmppsId, page, perPage)

    if (response.hasError(UpstreamApiError.Type.BAD_REQUEST)) {
      throw ValidationException("Bad request from upstream ${response.errors.first().description}")
    }
    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Entity not found ${response.errors.first().description}")
    }
    auditService.createEvent("GET_PERSON_CONTACT_EVENTS", mapOf("hmppsId" to hmppsId))
    return response.data.toPaginatedResponse()
  }

  @GetMapping("{contactEventId}")
  @Tag(name = "Contact Event")
  @Operation(
    summary = "Returns a Contact Event for a person and contact event id.",
    responses = [
      ApiResponse(
        responseCode = "200",
        useReturnTypeSchema = true,
        description = "Successfully found a person's contact event with the provided HMPPS ID and contact event id.",
      ),
      ApiResponse(
        responseCode = "400",
        content = [Content(schema = Schema(ref = "#/components/schemas/BadRequest"))],
      ),
      ApiResponse(
        responseCode = "404",
        content = [Content(schema = Schema(ref = "#/components/schemas/PersonNotFound"))],
      ),
      ApiResponse(
        responseCode = "500",
        content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))],
      ),
    ],
  )
  @FeatureFlag(name = FeatureFlagConfig.USE_CONTACT_EVENTS_ENDPOINT)
  fun getContactEvent(
    @Parameter(description = "A HMPPS identifier") @PathVariable hmppsId: String,
    @Parameter(description = "A Contact Event Id") @PathVariable contactEventId: Long,
  ): DataResponse<ContactEvent> {
    val response = contactEventService.getContactEvent(hmppsId, contactEventId)

    if (response.hasError(UpstreamApiError.Type.BAD_REQUEST)) {
      throw ValidationException("Bad request from upstream ${response.errors.first().description}")
    }
    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Entity not found ${response.errors.first().description}")
    }
    auditService.createEvent("GET_PERSON_CONTACT_EVENT", mapOf("hmppsId" to hmppsId))
    return DataResponse(response.data!!)
  }
}
