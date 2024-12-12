package uk.gov.justice.digital.hmpps.hmppsintegrationapi.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerConfig

@Configuration
@Component
@ConfigurationProperties(prefix = "authorisation")
class AuthorisationConfig {
  var consumers: Map<String, ConsumerConfig> = emptyMap()
}
