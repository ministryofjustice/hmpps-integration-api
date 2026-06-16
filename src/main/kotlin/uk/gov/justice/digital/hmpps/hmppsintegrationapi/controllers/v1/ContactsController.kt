package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.headers.Header
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ContactSearchRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ContactSearchResponseItem
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ContactSearchType
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DetailedContact
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.interfaces.toPaginatedResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.ContactSearchService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetContactService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.PaginatedResponse

@RestController
@RequestMapping("/v1/contacts")
@Tag(name = "Contacts")
class ContactsController(
  private val auditService: AuditService,
  private val getContactService: GetContactService,
  private val contactSearchService: ContactSearchService,
) {
  @GetMapping("/{contactId}")
  @Tag(name = "Visits")
  @Operation(
    summary = "Returns a contact by ID.",
    description = "",
    responses = [
      ApiResponse(responseCode = "200", useReturnTypeSchema = true, description = "Successfully found contact by contact ID."),
      ApiResponse(
        responseCode = "400",
        description = "The contact is in invalid format.",
        content = [Content(schema = Schema(ref = "#/components/schemas/BadRequest"))],
      ),
      ApiResponse(responseCode = "404", content = [Content(schema = Schema(ref = "#/components/schemas/PersonNotFound"))]),
      ApiResponse(responseCode = "500", content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))]),
    ],
  )
  fun getContactById(
    @PathVariable contactId: String,
  ): Response<DetailedContact?> {
    val response = getContactService.execute(contactId)

    if (response.hasError(UpstreamApiError.Type.BAD_REQUEST)) {
      throw ValidationException()
    }

    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException(contactId)
    }

    auditService.createEvent("GET_CONTACT_BY_ID", mapOf("contactId" to contactId))
    return response
  }

  @RequestMapping(method = [RequestMethod.GET, RequestMethod.POST])
  @Tag(name = "Visits")
  @Operation(
    summary = "Search for a contact.",
    description =
      "Search contacts by name and date of birth. Supports both GET and POST HTTP methods. <br>" +
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
  @FeatureFlag(name = FeatureFlagConfig.USE_CONTACT_SEARCH_ENDPOINT)
  fun contactSearch(
    @Parameter(description = "The first name of the contact") @RequestParam(required = false) firstName: String?,
    @Parameter(description = "The middle names of the contact") @RequestParam(required = false) middleNames: String?,
    @Parameter(description = "The last name of the contact") @RequestParam(required = false) lastName: String?,
    @Parameter(description = "The date of birth of the contact in the format (dd/mm/yyyy)") @RequestParam(required = false) dateOfBirth: String?,
    @Parameter(description = "The type of search (EXACT, PARTIAL, SOUNDS_LIKE (defaults to exact)") @RequestParam(required = false, defaultValue = "EXACT") searchType: ContactSearchType = ContactSearchType.EXACT,
    @Parameter(description = "The page number (starting at 1)", schema = Schema(minimum = "1")) @RequestParam(required = false, defaultValue = "1") pageNo: Int,
    @Parameter(description = "The maximum number of results for a page (starting at 1)", schema = Schema(minimum = "1")) @RequestParam(required = false, defaultValue = "10", name = "perPage") perPage: Int,
    @RequestAttribute requestContext: RequestContext?,
    @Valid @RequestBody request: ContactSearchRequest? = null,
  ): ResponseEntity<PaginatedResponse<ContactSearchResponseItem>> {
    val req = request ?: ContactSearchRequest(firstName, lastName, middleNames, dateOfBirth, searchType)
    req.validate()

    val response = contactSearchService.contactSearch(req, pageNo, perPage, requestContext)

    if (response.hasError(UpstreamApiError.Type.BAD_REQUEST)) {
      throw ValidationException()
    }

    auditService.createEvent("SEARCH_CONTACTS", req.toMap())

    return ResponseEntity
      .ok()
      .header(HttpHeaders.CACHE_CONTROL, "no-cache")
      .body(response.data.toPaginatedResponse())
  }
}
