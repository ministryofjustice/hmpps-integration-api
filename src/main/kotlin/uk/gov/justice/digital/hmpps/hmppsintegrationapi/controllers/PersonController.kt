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
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@RestController
@RequestMapping("/persons")
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
  ): Map<String, List<Person?>> {
    if (firstName == null && lastName == null) {
      throw ValidationException("No query parameters specified.")
    }

    val persons = getPersonsService.execute(firstName, lastName)

    return mapOf("persons" to persons)
  }

  @GetMapping("{encodedPncId}")
  fun getPerson(@PathVariable encodedPncId: String): Map<String, Person?> {
    val pncId = URLDecoder.decode(encodedPncId, StandardCharsets.UTF_8)
    val result = getPersonService.execute(pncId)

    if (result.isNullOrEmpty()) {
      throw EntityNotFoundException("Could not find person with id: $pncId")
    }

    return result
  }

  @GetMapping("{encodedPncId}/images")
  fun getPersonImages(@PathVariable encodedPncId: String): Map<String, List<ImageMetadata>> {
    val pncId = URLDecoder.decode(encodedPncId, StandardCharsets.UTF_8)
    val images = getImageMetadataForPersonService.execute(pncId)

    return mapOf("images" to images)
  }

  @GetMapping("{encodedPncId}/addresses")
  fun getPersonAddresses(@PathVariable encodedPncId: String): Map<String, List<Address>> {
    val pncId = URLDecoder.decode(encodedPncId, StandardCharsets.UTF_8)
    val addresses =
      getAddressesForPersonService.execute(pncId) ?: throw EntityNotFoundException("Could not find person with id: $pncId")

    return mapOf("addresses" to addresses)
  }
}
