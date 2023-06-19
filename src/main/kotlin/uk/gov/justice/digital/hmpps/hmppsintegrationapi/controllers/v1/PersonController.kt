package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1

import jakarta.validation.ValidationException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.decodeUrlCharacters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Address
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ImageMetadata
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApiError.Type.ENTITY_NOT_FOUND
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetAddressesForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetImageMetadataForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonsService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.PaginatedResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.paginateWith

@RestController
@RequestMapping("/v1/persons")
class PersonController(
  @Autowired val getPersonService: GetPersonService,
  @Autowired val getPersonsService: GetPersonsService,
  @Autowired val getImageMetadataForPersonService: GetImageMetadataForPersonService,
  @Autowired val getAddressesForPersonService: GetAddressesForPersonService,
) {

  @GetMapping
  fun getPersons(
    @RequestParam(required = false, name = "first_name") firstName: String?,
    @RequestParam(required = false, name = "last_name") lastName: String?,
    @RequestParam(required = false, defaultValue = "1", name = "page") page: Int,
    @RequestParam(required = false, defaultValue = "10", name = "perPage") perPage: Int,
  ): PaginatedResponse<Person?> {
    if (firstName == null && lastName == null) {
      throw ValidationException("No query parameters specified.")
    }

    val response = getPersonsService.execute(firstName, lastName)

    return response.data.paginateWith(page, perPage)
  }

  @GetMapping("{encodedPncId}")
  fun getPerson(@PathVariable encodedPncId: String): Map<String, Person?> {
    val pncId = encodedPncId.decodeUrlCharacters()
    val response = getPersonService.execute(pncId)

    if (
      response.hasErrorCausedBy(ENTITY_NOT_FOUND, causedBy = UpstreamApi.NOMIS) &&
      response.hasErrorCausedBy(ENTITY_NOT_FOUND, causedBy = UpstreamApi.PROBATION_OFFENDER_SEARCH)
    ) {
      throw EntityNotFoundException("Could not find person with id: $pncId")
    }

    return response.data
  }

  @GetMapping("{encodedPncId}/images")
  fun getPersonImages(
    @PathVariable encodedPncId: String,
    @RequestParam(required = false, defaultValue = "1", name = "page") page: Int,
    @RequestParam(required = false, defaultValue = "10", name = "perPage") perPage: Int,
  ): PaginatedResponse<ImageMetadata?> {
    val pncId = encodedPncId.decodeUrlCharacters()

    val response = getImageMetadataForPersonService.execute(pncId)

    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not find person with id: $pncId")
    }

    return response.data.paginateWith(page, perPage)
  }

  @GetMapping("{encodedPncId}/addresses")
  fun getPersonAddresses(@PathVariable encodedPncId: String): Map<String, List<Address>> {
    val pncId = encodedPncId.decodeUrlCharacters()
    val response = getAddressesForPersonService.execute(pncId)

    if (
      response.hasErrorCausedBy(ENTITY_NOT_FOUND, causedBy = UpstreamApi.NOMIS) &&
      response.hasErrorCausedBy(ENTITY_NOT_FOUND, causedBy = UpstreamApi.PROBATION_OFFENDER_SEARCH)
    ) {
      throw EntityNotFoundException("Could not find person with id: $pncId")
    }

    return mapOf("data" to response.data)
  }
}
