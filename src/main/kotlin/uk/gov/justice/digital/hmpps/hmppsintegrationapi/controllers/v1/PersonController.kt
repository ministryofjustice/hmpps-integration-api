package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1

import jakarta.validation.ValidationException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.decodeUrlCharacters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Address
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ImageMetadata
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetAddressesForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetImageMetadataForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonsService

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
     @PageableDefault pageable: Pageable,
  ): Page<Person?> {
    if (firstName == null && lastName == null) {
      throw ValidationException("No query parameters specified.")
    }

    val result = getPersonsService.execute(firstName, lastName)

    val start = pageable.pageSize * pageable.pageNumber
    val end = (start + pageable.pageSize).coerceAtMost(result.size)

    if (start > end) {
      return PageImpl(listOf<Person>(), pageable, result.size.toLong())
    }
    return PageImpl(result.subList(start, end), pageable, result.count().toLong())
  }

  @GetMapping("{encodedPncId}")
  fun getPerson(@PathVariable encodedPncId: String): Map<String, Person?> {
    val pncId = encodedPncId.decodeUrlCharacters()
    val result = getPersonService.execute(pncId)

    if (result.isNullOrEmpty()) {
      throw EntityNotFoundException("Could not find person with id: $pncId")
    }

    return result
  }

  @GetMapping("{encodedPncId}/images")
  fun getPersonImages(@PathVariable encodedPncId: String): Map<String, List<ImageMetadata>> {
    val pncId = encodedPncId.decodeUrlCharacters()
    val images = getImageMetadataForPersonService.execute(pncId)

    return mapOf("images" to images)
  }

  @GetMapping("{encodedPncId}/addresses")
  fun getPersonAddresses(@PathVariable encodedPncId: String): Map<String, List<Address>> {
    val pncId = encodedPncId.decodeUrlCharacters()
    val addresses = getAddressesForPersonService.execute(pncId)
      ?: throw EntityNotFoundException("Could not find person with id: $pncId")

    return mapOf("addresses" to addresses)
  }
}
