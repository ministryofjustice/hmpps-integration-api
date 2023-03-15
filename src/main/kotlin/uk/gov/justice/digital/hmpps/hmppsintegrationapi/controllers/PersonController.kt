package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers

import jakarta.validation.ValidationException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Address
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ImageMetadata
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetAddressesForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetImageMetadataForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonsService

@RestController
@RequestMapping("/persons")
class PersonController(
  @Autowired val getPersonService: GetPersonService,
  @Autowired val getPersonsService: GetPersonsService,
  @Autowired val getImageMetadataForPersonService: GetImageMetadataForPersonService,
  @Autowired val getAddressesForPersonService: GetAddressesForPersonService
) {

  @GetMapping
  fun getPersons(
    @RequestParam(required = false, name = "first_name") firstName: String?,
    @RequestParam(required = false, name = "last_name") lastName: String?
  ): Map<String, List<Person?>> {
    if (firstName == null && lastName == null) {
      throw ValidationException("No query parameters specified.")
    }

    val persons = getPersonsService.execute(firstName, lastName)

    return mapOf("persons" to persons)
  }

  @GetMapping("{decodedPncId}")
  fun getPerson(@PathVariable decodedPncId: String): Map<String, Person?> {
    val result = getPersonService.execute(decodedPncId)

    if (result.isNullOrEmpty()) {
      throw EntityNotFoundException("Could not find person with id: $decodedPncId")
    }

    return result
  }

  @GetMapping("{decodedPncId}/images")
  fun getPersonImages(@PathVariable decodedPncId: String): Map<String, List<ImageMetadata>> {
    val images = getImageMetadataForPersonService.execute(decodedPncId)

    return mapOf("images" to images)
  }

  @GetMapping("{decodedPncId}/addresses")
  fun getPersonAddresses(@PathVariable decodedPncId: String): Map<String, List<Address>> {
    val addresses =
      getAddressesForPersonService.execute(decodedPncId) ?: throw EntityNotFoundException("Could not find person with id: $decodedPncId")

    return mapOf("addresses" to addresses)
  }
}
