package uk.gov.justice.digital.hmpps.hmppsintegrationapi.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.Role
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.roles
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.MappaCategory

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

  /**
   * Merges the filters from the consumer config and roles
   */
  fun buildAggregatedFilters(
    consumerFilters: ConsumerFilters?,
    roles: List<Role>?,
  ): ConsumerFilters? {
    val consumerPseudoRole = Role(include = null, filters = consumerFilters)
    val allRoles: List<Role> = listOf(consumerPseudoRole) + (roles ?: emptyList())

    if (allRoles.all { it.filters?.hasFilters() == false }) {
      return null
    }

    val prisons =
      getDistinctValuesIfNotWildcarded(
        allRoles
          .filter { it.filters?.hasPrisonFilter() == true }
          .mapNotNull { it.filters?.prisons },
      )

    val caseNotes =
      getDistinctValuesIfNotWildcarded(
        allRoles
          .filter { it.filters?.hasCaseNotesFilter() == true }
          .mapNotNull { it.filters?.caseNotes },
      )

    val mappaCategories =
      getDistinctValuesForType<MappaCategory>(
        allRoles
          .filter { it.filters?.hasMappaCategoriesFilter() == true }
          .mapNotNull { it.filters?.mappaCategories },
      )

    return if (caseNotes == null && prisons == null && mappaCategories == null) null else ConsumerFilters(prisons, caseNotes, mappaCategories)
  }

  /**
   * Reduces a list of list<Any> (mixed) type to a flattened list of specified Enum type
   * If any of the items is a wild card then null is returned
   */
  private inline fun <reified T : Enum<T>> getDistinctValuesForType(allValues: List<List<Any>>): List<T>? =
    if (allValues.isEmpty()) {
      null
    } else {
      allValues
        .flatten()
        .map { toEnumIfExists<T>(it) }
        .distinct()
        .takeIf { it.none { value -> value is String && value == ("*") } }
        ?.filterIsInstance<T>()
    }

  /**
   * Converts string config into an enum class if exists
   */
  private inline fun <reified T : Enum<T>> toEnumIfExists(obj: Any): Any =
    if (obj is String && obj != ("*")) {
      runCatching { enumValueOf<T>(obj) }.getOrNull() ?: obj
    } else {
      obj
    }

  /**
   * Reduces a list of list<String> to a flattened list without wild cards
   * If any of the items is a wild card then null is returned
   */
  private fun getDistinctValuesIfNotWildcarded(allValues: List<List<String>>): List<String>? =
    if (allValues.isEmpty()) {
      null
    } else {
      allValues.flatten().distinct().takeIf { it.none { value -> value == "*" } }
    }
}
