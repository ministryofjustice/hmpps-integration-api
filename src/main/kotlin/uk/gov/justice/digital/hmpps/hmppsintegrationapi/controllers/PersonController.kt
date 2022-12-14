package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/persons")
class PersonController {

  @GetMapping("{id}")
  fun getPerson() {

  }
}