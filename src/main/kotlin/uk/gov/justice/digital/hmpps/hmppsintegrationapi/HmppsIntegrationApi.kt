package uk.gov.justice.digital.hmpps.hmppsintegrationapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class HmppsIntegrationApi

fun main(args: Array<String>) {
  runApplication<HmppsIntegrationApi>(*args)
}
