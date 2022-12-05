package uk.gov.justice.digital.hmpps.hmppsintegrationapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication()
class HmppsIntegrationApi

fun main(args: Array<String>) {
  runApplication<HmppsIntegrationApi>(*args)
}

@RestController
class TestApiController {
  @GetMapping("/")
  fun index(): String = "Test!"
}
