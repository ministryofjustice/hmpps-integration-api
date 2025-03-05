package uk.gov.justice.digital.hmpps.hmppsintegrationapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.PropertySource
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.YamlPropertySourceFactory

@SpringBootApplication
@PropertySource("classpath:globals.yml", factory = YamlPropertySourceFactory::class)
class HmppsIntegrationApi

fun main(args: Array<String>) {
  runApplication<HmppsIntegrationApi>(*args)
}
