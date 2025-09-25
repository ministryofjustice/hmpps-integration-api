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

  /**
   * Returns true if the consumer has access to the endpoint.
   */
  fun hasAccess(
    consumerName: String,
    endpoint: String,
  ): Boolean = anyMatch(allIncludes(consumerName), endpoint)

  /**
   * Returns a list of consumers with access to a particular endpoint.
   */
  fun consumersWithAccess(endpoint: String): List<String> =
    consumers
      .filter { hasAccess(it.key, endpoint) }
      .map { it.key }
      .toList()
      .sorted()

  /**
   * Returns a list of all endpoint permissions for a consumer, whether direct or from roles.
   */
  fun allIncludes(consumerName: String): List<String> {
    val merged = mutableSetOf<String>()
    merged.addAll(consumers[consumerName]?.include.orEmpty())
    for (roleName in consumers[consumerName]?.roles ?: emptyList()) {
      merged.addAll(roles[roleName]?.include.orEmpty())
    }
    return merged.toList().sorted()
  }

  /**
   * Returns true if the endpoint matches any of the patterns.
   */
  private fun anyMatch(
    patterns: List<String>?,
    endpoint: String,
  ): Boolean = patterns != null && patterns.any { Regex(it).matches(endpoint) }
}
