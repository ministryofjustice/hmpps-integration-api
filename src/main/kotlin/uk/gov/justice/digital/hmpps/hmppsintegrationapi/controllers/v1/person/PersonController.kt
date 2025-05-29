package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.tags.Tags
import jakarta.validation.ValidationException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.decodeUrlCharacters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.featureflag.FeatureFlag
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DataResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.IEPLevel
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ImageMetadata
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Language
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.NumberOfChildren
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.OffenderSearchResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonName
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonalCareNeed
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PhysicalCharacteristics
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonerContact
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError.Type.ENTITY_NOT_FOUND
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.VisitOrders
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.interfaces.toPaginatedResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetCareNeedsForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetIEPLevelService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetImageMetadataForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetLanguagesForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetNameForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetNumberOfChildrenForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonsService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPhysicalCharacteristicsForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPrisonerContactsService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetVisitOrdersForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.PaginatedResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.paginateWith
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RestController
@RequestMapping("/v1/persons")
@Tag(name = "Persons")
class PersonController(
  @Autowired val getPersonService: GetPersonService,
  @Autowired val getPersonsService: GetPersonsService,
  @Autowired val getNameForPersonService: GetNameForPersonService,
  @Autowired val getImageMetadataForPersonService: GetImageMetadataForPersonService,
  @Autowired val getPrisonerContactsService: GetPrisonerContactsService,
  @Autowired val getIEPLevelService: GetIEPLevelService,
  @Autowired val getVisitOrdersForPersonService: GetVisitOrdersForPersonService,
  @Autowired val getNumberOfChildrenForPersonService: GetNumberOfChildrenForPersonService,
  @Autowired val getPhysicalCharacteristicsForPersonService: GetPhysicalCharacteristicsForPersonService,
  @Autowired val getCareNeedsForPersonService: GetCareNeedsForPersonService,
  @Autowired val getLanguagesForPersonService: GetLanguagesForPersonService,
  @Autowired val auditService: AuditService,
  @Autowired val featureFlag: FeatureFlagConfig,
) {
  @GetMapping
  @Operation(
    summary = "Returns person(s) by search criteria, sorted by date of birth (newest first). At least one query parameter must be specified.",
    responses = [
      ApiResponse(responseCode = "200", useReturnTypeSchema = true, description = "Successfully performed the query on upstream APIs. An empty list is returned when no results are found."),
      ApiResponse(
        responseCode = "400",
        description = "There were no query parameters passed in. At least one must be specified.",
        content = [Content(schema = Schema(ref = "#/components/schemas/BadRequest"))],
      ),
      ApiResponse(responseCode = "500", content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))]),
    ],
  )
  fun getPersons(
    @Parameter(description = "The first name of the person") @RequestParam(required = false, name = "first_name") firstName: String?,
    @Parameter(description = "The last name of the person") @RequestParam(required = false, name = "last_name") lastName: String?,
    @Parameter(description = "A URL-encoded pnc identifier") @RequestParam(required = false, name = "pnc_number") pncNumber: String?,
    @Parameter(description = "The date of birth of the person") @RequestParam(required = false, name = "date_of_birth") dateOfBirth: String?,
    @Parameter(description = "Whether to return results that match the search criteria within the aliases of a person.") @RequestParam(required = false, defaultValue = "false", name = "search_within_aliases") searchWithinAliases: Boolean,
    @Parameter(description = "The page number (starting from 1)", schema = Schema(minimum = "1")) @RequestParam(required = false, defaultValue = "1", name = "page") page: Int,
    @Parameter(description = "The maximum number of results for a page", schema = Schema(minimum = "1")) @RequestParam(required = false, defaultValue = "10", name = "perPage") perPage: Int,
  ): PaginatedResponse<Person?> {
    if (firstName == null && lastName == null && pncNumber == null && dateOfBirth == null) {
      throw ValidationException("No query parameters specified.")
    }

    if (dateOfBirth != null && !isValidISODateFormat(dateOfBirth)) {
      throw ValidationException("Invalid date format. Please use yyyy-MM-dd.")
    }

    val response = getPersonsService.execute(firstName, lastName, pncNumber, dateOfBirth, searchWithinAliases)

    auditService.createEvent(
      "SEARCH_PERSON",
      mapOf("firstName" to firstName, "lastName" to lastName, "aliases" to searchWithinAliases.toString(), "pncNumber" to pncNumber, "dateOfBirth" to dateOfBirth),
    )
    return response.data.paginateWith(page, perPage)
  }

  @GetMapping("{encodedHmppsId}")
  @Operation(
    summary = "Returns a person.",
    responses = [
      ApiResponse(responseCode = "200", useReturnTypeSchema = true, description = "Successfully found a person with the provided HMPPS ID."),
      ApiResponse(responseCode = "404", content = [Content(schema = Schema(ref = "#/components/schemas/PersonNotFound"))]),
      ApiResponse(responseCode = "500", content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))]),
    ],
  )
  fun getPerson(
    @Parameter(description = "A URL-encoded HMPPS identifier", example = "2008%2F0545166T") @PathVariable encodedHmppsId: String,
  ): DataResponse<OffenderSearchResponse> {
    val hmppsId = encodedHmppsId.decodeUrlCharacters()
    val response = getPersonService.getCombinedDataForPerson(hmppsId)

    if (response.hasErrorCausedBy(ENTITY_NOT_FOUND, causedBy = UpstreamApi.NDELIUS)) {
      throw EntityNotFoundException("Could not find person with id: $hmppsId")
    }

    auditService.createEvent("GET_PERSON_DETAILS", mapOf("hmppsId" to hmppsId))
    val data = response.data
    return DataResponse(data)
  }

  @GetMapping("{hmppsId}/images")
  @Tags(value = [Tag(name = "Images"), Tag(name = "Reception")])
  @Operation(
    summary = "Returns metadata of images associated with a person sorted by captureDateTime (newest first).",
    description = "<b>Applicable filters</b>: <ul><li>prisons</li></ul>",
    responses = [
      ApiResponse(responseCode = "200", useReturnTypeSchema = true, description = "Successfully found a person with the provided HMPPS ID. If a person doesn't have any images, then an empty list (`[]`) is returned in the `data` property."),
      ApiResponse(responseCode = "400", content = [Content(schema = Schema(ref = "#/components/schemas/BadRequest"))]),
      ApiResponse(responseCode = "404", content = [Content(schema = Schema(ref = "#/components/schemas/PersonNotFound"))]),
      ApiResponse(responseCode = "500", content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))]),
    ],
  )
  fun getPersonImages(
    @Parameter(description = "A HMPPS identifier", example = "A1234AA") @PathVariable hmppsId: String,
    @RequestAttribute filters: ConsumerFilters?,
    @Parameter(description = "The page number (starting from 1)", schema = Schema(minimum = "1")) @RequestParam(required = false, defaultValue = "1", name = "page") page: Int,
    @Parameter(description = "The maximum number of results for a page", schema = Schema(minimum = "1")) @RequestParam(required = false, defaultValue = "10", name = "perPage") perPage: Int,
  ): PaginatedResponse<ImageMetadata?> {
    val response = getImageMetadataForPersonService.execute(hmppsId, filters)

    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not find person with id: $hmppsId")
    }

    if (response.hasError(UpstreamApiError.Type.BAD_REQUEST)) {
      throw ValidationException("Invalid HMPPS ID: $hmppsId")
    }

    auditService.createEvent("GET_PERSON_IMAGE", mapOf("hmppsId" to hmppsId))
    return response.data.paginateWith(page, perPage)
  }

  @GetMapping("{hmppsId}/name")
  @Operation(
    summary = "Returns a person's name",
    description = "<b>Applicable filters</b>: <ul><li>prisons</li></ul>",
    responses = [
      ApiResponse(responseCode = "200", useReturnTypeSchema = true, description = "Successfully found a person with the provided HMPPS ID."),
      ApiResponse(responseCode = "400", content = [Content(schema = Schema(ref = "#/components/schemas/BadRequest"))]),
      ApiResponse(responseCode = "404", content = [Content(schema = Schema(ref = "#/components/schemas/PersonNotFound"))]),
      ApiResponse(responseCode = "500", content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))]),
    ],
  )
  fun getPersonName(
    @Parameter(description = "The HMPPS ID of the person") @PathVariable hmppsId: String,
    @RequestAttribute filters: ConsumerFilters?,
  ): DataResponse<PersonName?> {
    val response = getNameForPersonService.execute(hmppsId, filters)

    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not find person with id: $hmppsId")
    }
    if (response.hasError(UpstreamApiError.Type.BAD_REQUEST)) {
      throw ValidationException("Invalid HMPPS ID: $hmppsId")
    }

    auditService.createEvent("GET_PERSON_NAME", mapOf("hmppsId" to hmppsId))
    return DataResponse(response.data)
  }

  @GetMapping("{hmppsId}/contacts")
  @Tags(value = [Tag(name = "Visits"), Tag(name = "Contacts"), Tag(name = "Reception")])
  @Operation(
    summary = "Returns a prisoners contacts.",
    description = "<b>Applicable filters</b>: <ul><li>prisons</li></ul>",
    responses = [
      ApiResponse(responseCode = "200", useReturnTypeSchema = true, description = "Successfully found a prisoner's contacts."),
      ApiResponse(responseCode = "404", content = [Content(schema = Schema(ref = "#/components/schemas/PersonNotFound"))]),
      ApiResponse(responseCode = "500", content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))]),
    ],
  )
  fun getPrisonersContacts(
    @Parameter(description = "The HMPPS ID of the prisoner") @PathVariable hmppsId: String,
    @Parameter(description = "The page number (starting from 1)", schema = Schema(minimum = "1")) @RequestParam(required = false, defaultValue = "1", name = "page") page: Int,
    @Parameter(description = "The maximum number of results for a page", schema = Schema(minimum = "1")) @RequestParam(required = false, defaultValue = "10", name = "perPage") perPage: Int,
    @RequestAttribute filters: ConsumerFilters?,
  ): PaginatedResponse<PrisonerContact> {
    val response = getPrisonerContactsService.execute(hmppsId, page, perPage, filters)

    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not find person with id: $hmppsId")
    }

    if (response.hasError(UpstreamApiError.Type.BAD_REQUEST)) {
      throw ValidationException("Invalid HMPPS ID: $hmppsId")
    }

    auditService.createEvent("GET_PRISONER_CONTACTS", mapOf("hmppsId" to hmppsId))

    return response.data.toPaginatedResponse()
  }

  @GetMapping("{hmppsId}/iep-level")
  @Tag(name = "Visits")
  @Operation(
    summary = "Returns a prisoners IEP level.",
    description = "<b>Applicable filters</b>: <ul><li>prisons</li></ul>",
    responses = [
      ApiResponse(responseCode = "200", useReturnTypeSchema = true, description = "Successfully found a prisoner's IEP level."),
      ApiResponse(responseCode = "400", content = [Content(schema = Schema(ref = "#/components/schemas/BadRequest"))]),
      ApiResponse(responseCode = "404", content = [Content(schema = Schema(ref = "#/components/schemas/PersonNotFound"))]),
      ApiResponse(responseCode = "500", content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))]),
    ],
  )
  fun getPrisonersIEPLevel(
    @Parameter(description = "The HMPPS ID of the prisoner") @PathVariable hmppsId: String,
    @RequestAttribute filters: ConsumerFilters?,
  ): DataResponse<IEPLevel?> {
    val response = getIEPLevelService.execute(hmppsId, filters)

    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not find person with id: $hmppsId")
    }

    if (response.hasError(UpstreamApiError.Type.BAD_REQUEST)) {
      throw ValidationException("Invalid HMPPS ID: $hmppsId")
    }

    auditService.createEvent("GET_PRISONER_IEP_LEVEL", mapOf("hmppsId" to hmppsId))

    return DataResponse(data = response.data)
  }

  @GetMapping("{hmppsId}/visit-orders")
  @Tags(value = [Tag(name = "Visits"), Tag(name = "Reception")])
  @Operation(
    summary = "Returns the number of remaining visit orders a prisoner has.",
    responses = [
      ApiResponse(responseCode = "200", useReturnTypeSchema = true, description = "Successfully found a prisoner's visit orders."),
      ApiResponse(responseCode = "404", content = [Content(schema = Schema(ref = "#/components/schemas/PersonNotFound"))]),
      ApiResponse(responseCode = "500", content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))]),
    ],
  )
  fun getPrisonersVisitOrders(
    @Parameter(description = "The HMPPS ID of the prisoner") @PathVariable hmppsId: String,
    @RequestAttribute filters: ConsumerFilters?,
  ): DataResponse<VisitOrders?> {
    val response = getVisitOrdersForPersonService.execute(hmppsId, filters)

    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not find person with id: $hmppsId")
    }

    if (response.hasError(UpstreamApiError.Type.BAD_REQUEST)) {
      throw ValidationException("Invalid HMPPS ID: $hmppsId")
    }

    auditService.createEvent("GET_PRISONER_VISIT_ORDERS", mapOf("hmppsId" to hmppsId))

    return DataResponse(response.data)
  }

  @GetMapping("{hmppsId}/number-of-children")
  @Tag(name = "Reception")
  @Operation(
    summary = "Returns a prisoner's number of children.",
    description = "<b>Applicable filters</b>: <ul><li>prisons</li></ul>",
    responses = [
      ApiResponse(responseCode = "200", useReturnTypeSchema = true, description = "Successfully found a prisoner's number of children."),
      ApiResponse(responseCode = "400", content = [Content(schema = Schema(ref = "#/components/schemas/BadRequest"))]),
      ApiResponse(responseCode = "404", content = [Content(schema = Schema(ref = "#/components/schemas/PersonNotFound"))]),
      ApiResponse(responseCode = "500", content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))]),
    ],
  )
  fun getPrisonersNumberofChildren(
    @Parameter(description = "The HMPPS ID of the prisoner") @PathVariable hmppsId: String,
    @RequestAttribute filters: ConsumerFilters?,
  ): DataResponse<NumberOfChildren?> {
    val response = getNumberOfChildrenForPersonService.execute(hmppsId, filters)

    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not find person with id: $hmppsId")
    }

    if (response.hasError(UpstreamApiError.Type.BAD_REQUEST)) {
      throw ValidationException("Invalid HMPPS ID: $hmppsId")
    }

    auditService.createEvent("GET_PRISONER_NUMBER_OF_CHILDREN", mapOf("hmppsId" to hmppsId))

    return DataResponse(data = response.data)
  }

  @GetMapping("{hmppsId}/physical-characteristics")
  @Tag(name = "Reception")
  @Operation(
    summary = "Gets physical characteristics and distinguishing marks for a prisoner.",
    description = "<b>Applicable filters</b>: <ul><li>prisons</li></ul>",
    responses = [
      ApiResponse(responseCode = "200", useReturnTypeSchema = true, description = "Successfully found a person's physical characteristics with the provided HMPPS ID."),
      ApiResponse(responseCode = "400", content = [Content(schema = Schema(ref = "#/components/schemas/BadRequest"))]),
      ApiResponse(responseCode = "404", content = [Content(schema = Schema(ref = "#/components/schemas/PersonNotFound"))]),
      ApiResponse(responseCode = "500", content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))]),
    ],
  )
  fun getPhysicalCharacteristicsForPerson(
    @Parameter(description = "A HMPPS identifier") @PathVariable hmppsId: String,
    @RequestAttribute filters: ConsumerFilters?,
  ): DataResponse<PhysicalCharacteristics?> {
    featureFlag.require(FeatureFlagConfig.USE_PHYSICAL_CHARACTERISTICS_ENDPOINTS)

    val response = getPhysicalCharacteristicsForPersonService.execute(hmppsId, filters = filters)

    if (response.hasError(UpstreamApiError.Type.BAD_REQUEST)) {
      throw ValidationException("Bad request from upstream ${response.errors.first().description}")
    }
    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not find person with id: $hmppsId")
    }

    auditService.createEvent("GET_PERSON_PHYSICAL_CHARACTERISTICS", mapOf("hmppsId" to hmppsId))
    return DataResponse(response.data)
  }

  @GetMapping("{hmppsId}/care-needs")
  @Tag(name = "Reception")
  @Operation(
    summary = "Gets care needs for a prisoner.",
    description = "<b>Applicable filters</b>: <ul><li>prisons</li></ul>",
    responses = [
      ApiResponse(responseCode = "200", useReturnTypeSchema = true, description = "Successfully found a person's care needs with the provided HMPPS ID."),
      ApiResponse(responseCode = "400", content = [Content(schema = Schema(ref = "#/components/schemas/BadRequest"))]),
      ApiResponse(responseCode = "404", content = [Content(schema = Schema(ref = "#/components/schemas/PersonNotFound"))]),
      ApiResponse(responseCode = "500", content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))]),
    ],
  )
  @FeatureFlag(name = FeatureFlagConfig.USE_PERSONAL_CARE_NEEDS_ENDPOINTS)
  fun getCareNeedsForPerson(
    @Parameter(description = "A HMPPS identifier") @PathVariable hmppsId: String,
    @RequestAttribute filters: ConsumerFilters?,
  ): DataResponse<List<PersonalCareNeed>?> {
    val response = getCareNeedsForPersonService.execute(hmppsId, filters = filters)

    if (response.hasError(UpstreamApiError.Type.BAD_REQUEST)) {
      throw ValidationException("Bad request from upstream ${response.errors.first().description}")
    }
    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not find person with id: $hmppsId")
    }

    auditService.createEvent("GET_PERSON_CARE_NEEDS", mapOf("hmppsId" to hmppsId))
    return DataResponse(response.data)
  }

  @GetMapping("{hmppsId}/languages")
  @Tag(name = "Reception")
  @Operation(
    summary = "Gets languages for a prisoner.",
    description = "<b>Applicable filters</b>: <ul><li>prisons</li></ul>",
    responses = [
      ApiResponse(responseCode = "200", useReturnTypeSchema = true, description = "Successfully found a person's Language information with the provided HMPPS ID."),
      ApiResponse(responseCode = "400", content = [Content(schema = Schema(ref = "#/components/schemas/BadRequest"))]),
      ApiResponse(responseCode = "404", content = [Content(schema = Schema(ref = "#/components/schemas/PersonNotFound"))]),
      ApiResponse(responseCode = "500", content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))]),
    ],
  )
  @FeatureFlag(name = FeatureFlagConfig.USE_LANGUAGES_ENDPOINTS)
  fun getLanguagesForPerson(
    @Parameter(description = "A HMPPS identifier") @PathVariable hmppsId: String,
    @RequestAttribute filters: ConsumerFilters?,
  ): DataResponse<List<Language>?> {
    val response = getLanguagesForPersonService.execute(hmppsId, filters = filters)

    if (response.hasError(UpstreamApiError.Type.BAD_REQUEST)) {
      throw ValidationException("Bad request from upstream ${response.errors.first().description}")
    }
    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not find person with id: $hmppsId")
    }

    auditService.createEvent("GET_LANGUAGES", mapOf("hmppsId" to hmppsId))
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
