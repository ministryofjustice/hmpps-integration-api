package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonService

@RestController
@RequestMapping("/persons")
class PersonController(@Autowired val getPersonService: GetPersonService) {

  @GetMapping("{id}")
  fun getPerson(@PathVariable id: Int): Person? {
    val result = getPersonService.execute(id)

    println(result)

    if(result == null) {
      println("yes we are null")
      throw ResponseStatusException(HttpStatus.NOT_FOUND, "");
    }

    return result
  }
}
