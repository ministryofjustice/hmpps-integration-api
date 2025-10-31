package uk.gov.justice.digital.hmpps.hmppsintegrationapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig

@SpringBootApplication
@EnableConfigurationProperties(FeatureFlagConfig::class)
class HmppsIntegrationApi

fun main(args: Array<String>) {
  runApplication<HmppsIntegrationApi>(*args)
}
