package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ImageMetadata
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetImageMetadataForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonService

@RestController
@RequestMapping("/persons")
class PersonController(
  @Autowired val getPersonService: GetPersonService,
  @Autowired val getImageMetadataForPersonService: GetImageMetadataForPersonService
) {

  @GetMapping("")
  fun getPerson(@RequestParam firstName: String, @RequestParam lastName: String): Map<String, List<Person>> {
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
}
