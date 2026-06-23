package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.headers.Header
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.tags.Tags
import jakarta.validation.Valid
import jakarta.validation.ValidationException
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.featureflag.FeatureFlag
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Address
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.AddressSearchRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.AddressSearchResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DataResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.AddressSearchService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetAddressesForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

@RestController
@RequestMapping("/v1")
@Tag(name = "Address")
class AddressController(
  private val auditService: AuditService,
  private val getAddressesForPersonService: GetAddressesForPersonService,
  private val addressSearchService: AddressSearchService,
) {
  @GetMapping("persons/{hmppsId}/addresses")
  @Tags(Tag(name = "Persons"), Tag(name = "Reception"))
  @Operation(
    summary = "Returns addresses associated with a person, ordered by startDate.",
    description = "<b>Applicable filters</b>: <ul><li>prisons</li></ul>",
    responses = [
      ApiResponse(responseCode = "200", useReturnTypeSchema = true, description = "Successfully found a person with the provided HMPPS ID."),
      ApiResponse(responseCode = "400", content = [Content(schema = Schema(ref = "#/components/schemas/BadRequest"))]),
      ApiResponse(responseCode = "404", content = [Content(schema = Schema(ref = "#/components/schemas/PersonNotFound"))]),
      ApiResponse(responseCode = "500", content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))]),
    ],
  )
  fun getPersonAddresses(
    @Parameter(description = "The HMPPS ID of the person", example = "G2996UX") @PathVariable hmppsId: String,
    @RequestAttribute requestContext: RequestContext?,
  ): DataResponse<List<Address>> {
    val response = getAddressesForPersonService.execute(hmppsId, requestContext?.filters)

    if (response.hasError(UpstreamApiError.Type.BAD_REQUEST)) {
      throw ValidationException("Invalid id: $hmppsId")
    }

    if (response.data.isEmpty() && response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not find person with id: $hmppsId")
    }

    auditService.createEvent("GET_PERSON_ADDRESS", mapOf("hmppsId" to hmppsId))

    return DataResponse(response.data)
  }

  @RequestMapping(method = [RequestMethod.GET, RequestMethod.POST], value = ["addresses"])
  @Operation(
    summary = "Search for a address.",
    description =
      "Search address by building name, address number, street name and postcode. Supports both GET and POST HTTP methods. <br>" +
        "If a request body is provided, then the search criteria query params will be ignored",
    responses = [
      ApiResponse(
        responseCode = "200",
        useReturnTypeSchema = true,
        description = "Search completed successfully.",
        headers = [Header(name = HttpHeaders.CACHE_CONTROL, description = "set to no-cache")],
      ),
      ApiResponse(
        responseCode = "400",
        description = "Invalid Request.",
        content = [Content(schema = Schema(ref = "#/components/schemas/BadRequest"))],
      ),
      ApiResponse(responseCode = "500", content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))]),
    ],
  )
  @FeatureFlag(name = FeatureFlagConfig.ADDRESS_SEARCH_ENDPOINT_ENABLED)
  fun getAddressSearch(
    @Parameter(description = "The building name of the address") @RequestParam(required = false) buildingName: String?,
    @Parameter(description = "The address number of the address") @RequestParam(required = false) addressNumber: String?,
    @Parameter(description = "The street name of the address") @RequestParam(required = false) streetName: String?,
    @Parameter(description = "The postcode name of the address") @RequestParam(required = false) postcode: String?,
    @Parameter(description = "Maximum number of search results") @RequestParam(required = false, defaultValue = "100") maxResults: Int,
    @RequestAttribute requestContext: RequestContext?,
    @Valid @RequestBody request: AddressSearchRequest? = null,
  ): ResponseEntity<AddressSearchResponse> {
    val req = request ?: AddressSearchRequest(buildingName, addressNumber, streetName, postcode)
    req.validate()

    val response = addressSearchService.addressSearch(req, maxResults, requestContext)

    if (response.hasError(UpstreamApiError.Type.BAD_REQUEST)) {
      throw ValidationException()
    }

    auditService.createEvent("SEARCH_ADDRESS", req.toMap())

    return ResponseEntity
      .ok()
      .header(HttpHeaders.CACHE_CONTROL, "no-cache")
      .body(response.data)
  }
}
