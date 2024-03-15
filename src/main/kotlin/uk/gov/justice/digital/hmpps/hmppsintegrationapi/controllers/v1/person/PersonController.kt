package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import jakarta.validation.ValidationException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.decodeUrlCharacters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ImageMetadata
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError.Type.ENTITY_NOT_FOUND
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetImageMetadataForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonsService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.PaginatedResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.paginateWith
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RestController
@RequestMapping("/v1/persons")
class PersonController(
  @Autowired val getPersonService: GetPersonService,
  @Autowired val getPersonsService: GetPersonsService,
  @Autowired val getImageMetadataForPersonService: GetImageMetadataForPersonService,
  @Autowired val auditService: AuditService,
) {

  @GetMapping
  fun getPersons(
    @RequestParam(required = false, name = "first_name") firstName: String?,
    @RequestParam(required = false, name = "last_name") lastName: String?,
    @RequestParam(required = false, name = "pnc_number") pncNumber: String?,
    @RequestParam(required = false, name = "date_of_birth") dateOfBirth: String?,
    @RequestParam(required = false, defaultValue = "false", name = "search_within_aliases") searchWithinAliases: Boolean,
    @RequestParam(required = false, defaultValue = "1", name = "page") page: Int,
    @RequestParam(required = false, defaultValue = "10", name = "perPage") perPage: Int,
  ): PaginatedResponse<Person?> {
    if (firstName == null && lastName == null && pncNumber == null && dateOfBirth == null) {
      throw ValidationException("No query parameters specified.")
    }

    if (dateOfBirth != null && !isValidISODateFormat(dateOfBirth)) {
      throw ValidationException("Invalid date format. Please use yyyy-MM-dd.")
    }

    val response = getPersonsService.execute(firstName, lastName, pncNumber, dateOfBirth, searchWithinAliases)

    auditService.createEvent("SEARCH_PERSON", "Person searched with first name: $firstName, last name: $lastName, search within aliases: $searchWithinAliases, pnc number: $pncNumber, date of birth: $dateOfBirth")
    return response.data.paginateWith(page, perPage, response.errors)

  }

  @GetMapping("{encodedHmppsId}")
  fun getPerson(@PathVariable encodedHmppsId: String): Map<String, Person?> {
    val hmppsId = encodedHmppsId.decodeUrlCharacters()
    val response = getPersonService.execute(hmppsId)

    if (response.hasErrorCausedBy(ENTITY_NOT_FOUND, causedBy = UpstreamApi.PROBATION_OFFENDER_SEARCH)) {
      throw EntityNotFoundException("Could not find person with id: $hmppsId")
    }

    auditService.createEvent("GET_PERSON_DETAILS", "Person details with hmpps id: $hmppsId has been retrieved")
    return mapOf("data" to response.data)
  }

  @GetMapping("{encodedHmppsId}/images")
  fun getPersonImages(
    @PathVariable encodedHmppsId: String,
    @RequestParam(required = false, defaultValue = "1", name = "page") page: Int,
    @RequestParam(required = false, defaultValue = "10", name = "perPage") perPage: Int,
  ): PaginatedResponse<ImageMetadata?> {
    val hmppsId = encodedHmppsId.decodeUrlCharacters()

    val response = getImageMetadataForPersonService.execute(hmppsId)

    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not find person with id: $hmppsId")
    }

    auditService.createEvent("GET_PERSON_IMAGE", "Image with id: $hmppsId has been retrieved")
    return response.data.paginateWith(page, perPage)
  }

  private fun isValidISODateFormat(dateString: String): Boolean {
    return try {
      LocalDate.parse(dateString, DateTimeFormatter.ISO_DATE)
      true
    } catch (e: Exception) {
      false
    }
  }
}
