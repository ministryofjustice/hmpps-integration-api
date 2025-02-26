package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.prison

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
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.ForbiddenByUpstreamServiceException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DataResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonInPrison
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError.Type.BAD_REQUEST
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError.Type.ENTITY_NOT_FOUND
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError.Type.FORBIDDEN
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonVisits.PaginatedVisit
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPrisonersService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetVisitsService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.PaginatedResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.paginateWith
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RestController
@RequestMapping("/v1/prison")
@Tag(name = "prison")
class PrisonController(
  @Autowired val getPrisonersService: GetPrisonersService,
  @Autowired val getPersonService: GetPersonService,
  @Autowired val getVisitsService: GetVisitsService,
  @Autowired val auditService: AuditService,
) {
  @GetMapping("/prisoners/{hmppsId}")
  @Operation(
    summary = "Returns a single prisoners details given an hmppsId, does not query for a probation person.",
    description = "<b>Applicable filters</b>: <ul><li>prisons</li></ul>",
    responses = [
      ApiResponse(responseCode = "200", useReturnTypeSchema = true, description = "Successfully found a prisoner with the provided HMPPS ID."),
      ApiResponse(
        responseCode = "400",
        description = "The HMPPS ID provided has an invalid format.",
        content = [Content(schema = Schema(ref = "#/components/schemas/BadRequest"))],
      ),
      ApiResponse(responseCode = "404", content = [Content(schema = Schema(ref = "#/components/schemas/PersonNotFound"))]),
      ApiResponse(responseCode = "500", content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))]),
    ],
  )
  fun getPerson(
    @Parameter(description = "The HMPPS ID of the person") @PathVariable hmppsId: String,
    @RequestAttribute filters: ConsumerFilters?,
  ): DataResponse<PersonInPrison?> {
    val response = getPersonService.getPrisoner(hmppsId, filters)

    if (response.hasErrorCausedBy(BAD_REQUEST, causedBy = UpstreamApi.NOMIS)) {
      throw ValidationException("Invalid HMPPS ID: $hmppsId")
    }
    if (response.hasErrorCausedBy(ENTITY_NOT_FOUND, causedBy = UpstreamApi.PRISONER_OFFENDER_SEARCH)) {
      throw EntityNotFoundException("Could not find person with hmppsId: $hmppsId")
    }

    auditService.createEvent("GET_PERSON_DETAILS", mapOf("hmppsId" to hmppsId))
    val data = response.data
    return DataResponse(data)
  }

  @GetMapping("/prisoners")
  @Operation(
    summary = "Returns person(s) by search criteria, sorted by date of birth (newest first). Only queries prisoner search.",
    description = "<b>Applicable filters</b>: <ul><li>prisons</li></ul>",
    responses = [
      ApiResponse(responseCode = "200", useReturnTypeSchema = true, description = "Successfully performed the query on upstream APIs. An empty list is returned when no results are found."),
      ApiResponse(
        responseCode = "400",
        description = "There were no query parameters passed in. At least one must be specified.",
        content = [Content(schema = Schema(ref = "#/components/schemas/BadRequest"))],
      ),
      ApiResponse(responseCode = "403", content = [Content(schema = Schema(ref = "#/components/schemas/ForbiddenResponse"))]),
      ApiResponse(responseCode = "500", content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))]),
    ],
  )
  fun getPrisoners(
    @Parameter(description = "The first name of the person") @RequestParam(required = false, name = "first_name") firstName: String?,
    @Parameter(description = "The last name of the person") @RequestParam(required = false, name = "last_name") lastName: String?,
    @Parameter(description = "The date of birth of the person") @RequestParam(required = false, name = "date_of_birth") dateOfBirth: String?,
    @Parameter(description = "Whether to return results that match the search criteria within the aliases of a person.") @RequestParam(required = false, defaultValue = "false", name = "search_within_aliases") searchWithinAliases: Boolean,
    @Parameter(description = "The page number (starting from 1)", schema = Schema(minimum = "1")) @RequestParam(required = false, defaultValue = "1", name = "page") page: Int,
    @Parameter(description = "The maximum number of results for a page", schema = Schema(minimum = "1")) @RequestParam(required = false, defaultValue = "10", name = "perPage") perPage: Int,
    @RequestAttribute filters: ConsumerFilters?,
  ): PaginatedResponse<PersonInPrison?> {
    if (firstName == null && lastName == null && dateOfBirth == null) {
      throw ValidationException("No query parameters specified.")
    }

    if (dateOfBirth != null && !isValidISODateFormat(dateOfBirth)) {
      throw ValidationException("Invalid date format. Please use yyyy-MM-dd.")
    }

    val response = getPrisonersService.execute(firstName, lastName, dateOfBirth, searchWithinAliases, filters)

    auditService.createEvent(
      "SEARCH_PERSON",
      mapOf("firstName" to firstName, "lastName" to lastName, "aliases" to searchWithinAliases.toString(), "dateOfBirth" to dateOfBirth),
    )

    if (response.hasErrorCausedBy(FORBIDDEN, causedBy = UpstreamApi.PRISONER_OFFENDER_SEARCH)) {
      throw ForbiddenByUpstreamServiceException("Consumer configured with no access to any prisons: ${filters?.prisons}")
    }

    if (response.hasErrorCausedBy(ENTITY_NOT_FOUND, causedBy = UpstreamApi.PRISONER_OFFENDER_SEARCH)) {
      throw EntityNotFoundException("Could not find persons with supplied query parameters: $firstName, $lastName, $dateOfBirth")
    }

    return response.data.paginateWith(page, perPage)
  }

  @GetMapping("/{prisonId}/visit/search")
  @Tag(name = "visits")
  @Operation(
    summary = "Searches for visits by prisonId and criteria.",
    description = "<b>Applicable filters</b>: <ul><li>prisons</li></ul>",
    responses = [
      ApiResponse(responseCode = "200", useReturnTypeSchema = true, description = "Successfully performed the query on upstream APIs. An empty list is returned when no results are found."),
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
  fun getVisits(
    @Parameter(description = "The ID of the prison to be queried against") @PathVariable prisonId: String,
    @Parameter(description = "The HMPPS ID of the person") @RequestParam(required = false) hmppsId: String?,
    @Parameter(description = "The start date of the visit") @RequestParam(required = false) fromDate: String?,
    @Parameter(description = "The end date of the visit") @RequestParam(required = false) toDate: String?,
    @Parameter(description = "The status of the visit. (BOOKING or CANCELLED)") @RequestParam visitStatus: String,
    @Parameter(description = "The page number", schema = Schema(minimum = "1")) @RequestParam(required = true, defaultValue = "1") page: Int,
    @Parameter(description = "The maximum number of results for a page", schema = Schema(minimum = "1")) @RequestParam(required = true, defaultValue = "10") size: Int,
    @RequestAttribute filters: ConsumerFilters?,
  ): DataResponse<PaginatedVisit?> {
    val response = getVisitsService.execute(hmppsId, prisonId, fromDate, toDate, visitStatus, page, size, filters)

    if (response.hasErrorCausedBy(BAD_REQUEST, causedBy = UpstreamApi.MANAGE_PRISON_VISITS)) {
      throw ValidationException("Invalid query parameters.")
    }

    if (response.hasErrorCausedBy(ENTITY_NOT_FOUND, causedBy = UpstreamApi.MANAGE_PRISON_VISITS)) {
      throw EntityNotFoundException("Could not find visits with supplied query parameters.")
    }

    auditService.createEvent(
      "SEARCH_VISITS",
      mapOf("prisonId" to prisonId, "hmppsId" to hmppsId, "fromDate" to fromDate, "toDate" to toDate, "visitStatus" to visitStatus),
    )

    return DataResponse(response.data)
  }

  private fun isValidISODateFormat(dateString: String): Boolean =
    try {
      LocalDate.parse(dateString, DateTimeFormatter.ISO_DATE)
      true
    } catch (e: Exception) {
      false
    }
}
