package uk.gov.justice.digital.hmpps.hmppsintegrationapi.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "redaction")
data class RedactionConfig(
  val clientNames: Set<String> = emptySet(),
)
