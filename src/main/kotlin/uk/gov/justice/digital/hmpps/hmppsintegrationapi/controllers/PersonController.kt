package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonService

@RestController
@RequestMapping("/persons")
class PersonController(@Autowired val getPersonService: GetPersonService) {

  @GetMapping("{id}")
  fun getPerson(@PathVariable id: Int): Person? {
    val result = getPersonService.execute(id) ?: throw EntityNotFoundException("Could not find person with id: ${id}")

    return result
  }
}