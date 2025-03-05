package uk.gov.justice.digital.hmpps.hmppsintegrationapi.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.Role

@Configuration
@ConfigurationProperties(prefix = "data")
data class RolesConfig(
  val roles: List<Role>,
)
