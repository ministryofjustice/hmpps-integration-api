package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v2

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.tags.Tags
import jakarta.validation.Valid
import jakarta.validation.ValidationException
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.featureflag.FeatureFlag
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.cpr.CorePersonRecordSearchRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.cpr.CorePersonRecordSearchResponseItem
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DataResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.v2.PersonSearchService

@RestController
@RequestMapping("/v2/persons")
@Tag(name = "Person")
class PersonSearchController(
  private val auditService: AuditService,
  private val personSearchService: PersonSearchService,
) {
  @FeatureFlag(name = FeatureFlagConfig.PERSON_SEARCH_V2_ENABLED)
  @PostMapping
  @Tags(Tag(name = "Person Search"))
  @Operation(
    summary = "Returns person(s) by search criteria",
    responses = [
      ApiResponse(responseCode = "200", useReturnTypeSchema = true, description = "Successfully found a person with the provided HMPPS ID."),
      ApiResponse(responseCode = "400", content = [Content(schema = Schema(ref = "#/components/schemas/BadRequest"))]),
      ApiResponse(responseCode = "500", content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))]),
    ],
  )
  fun personSearch(
    @RequestAttribute requestContext: RequestContext?,
    @Valid @RequestBody request: CorePersonRecordSearchRequest,
  ): DataResponse<List<CorePersonRecordSearchResponseItem>> {
    val response = personSearchService.personSearch(request, requestContext)

    if (response.hasError(UpstreamApiError.Type.BAD_REQUEST)) {
      throw ValidationException("Invalid request")
    }

    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Upstream returns not found")
    }
    auditService.createEvent("PERSON_SEARCH_V2", request.toAuditableMap())
    return DataResponse(response.data ?: emptyList())
  }
}
