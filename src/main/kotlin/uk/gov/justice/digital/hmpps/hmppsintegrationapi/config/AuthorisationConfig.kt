package uk.gov.justice.digital.hmpps.hmppsintegrationapi.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.roles

@Configuration
@Component
@ConfigurationProperties(prefix = "authorisation")
class AuthorisationConfig {
  var consumers: Map<String, ConsumerConfig?> = emptyMap()

  fun hasAccess(
    consumerName: String,
    endpoint: String,
  ): Boolean {
    val config = consumers[consumerName]
    if (anyMatch(config?.include, endpoint)) return true
    for (roleName in config?.roles ?: emptyList()) {
      if (roleCanAccess(roleName, endpoint)) return true
    }
    return false
  }

  fun consumersWithAccess(endpoint: String): List<String> =
    consumers
      .filter { hasAccess(it.key, endpoint) }
      .map { it.key }
      .toList()
      .sorted()

  private fun anyMatch(
    patterns: List<String>?,
    endpoint: String,
  ): Boolean = patterns != null && patterns.any { Regex(it).matches(endpoint) }

  private fun roleCanAccess(
    roleName: String,
    endpoint: String,
  ): Boolean = anyMatch(roles[roleName]?.include, endpoint)
}
