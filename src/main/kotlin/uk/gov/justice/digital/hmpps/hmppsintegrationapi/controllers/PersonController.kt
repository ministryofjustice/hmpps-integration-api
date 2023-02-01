package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonService

@RestController
@RequestMapping("/persons")
class PersonController(@Autowired val getPersonService: GetPersonService) {
  @GetMapping("{id}")
  fun getPerson(@PathVariable id: String): Map<String, Person?> {
    val result = getPersonService.execute(id) ?: throw EntityNotFoundException("Could not find person with id: $id")

    return result
  }
}
