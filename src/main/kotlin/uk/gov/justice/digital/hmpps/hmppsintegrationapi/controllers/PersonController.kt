package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers

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

@RestController
@RequestMapping("/persons")
class PersonController(
  @Autowired val getPersonService: GetPersonService,
  @Autowired val getImageMetadataForPersonService: GetImageMetadataForPersonService,
  @Autowired val getAddressesForPersonService: GetAddressesForPersonService
) {

  @GetMapping
  fun getPerson(@RequestParam firstName: String, @RequestParam lastName: String): Map<String, List<Person?>> {
    val persons = getPersonService.execute(firstName, lastName)

    return mapOf("persons" to persons)
  }

  @GetMapping("{id}")
  fun getPerson(@PathVariable id: String): Map<String, Person?> {
    val result = getPersonService.execute(id)

    if (result.isNullOrEmpty()) {
      throw EntityNotFoundException("Could not find person with id: $id")
    }

    return result
  }

  @GetMapping("{id}/images")
  fun getPersonImages(@PathVariable id: String): Map<String, List<ImageMetadata>> {
    val images = getImageMetadataForPersonService.execute(id)

    return mapOf("images" to images)
  }

  @GetMapping("{id}/addresses")
  fun getPersonAddresses(@PathVariable id: String): Map<String, List<Address>> {
    val addresses = getAddressesForPersonService.execute(id)

    return mapOf("addresses" to addresses)
  }
}
