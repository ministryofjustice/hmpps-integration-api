package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.AuthorisationConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.enums.IntegrationEventType
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.normalisePath
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.Role
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.MappaCategory
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.onbehalfof.EntraJwtOboService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.onbehalfof.OboService

@Component
class AuthorisationService(
  private val authorisationConfig: AuthorisationConfig,
) {
  fun authorised(
    consumerName: String,
    requestedPath: String,
  ): Boolean =
    authorisedThroughIncludes(consumerName, requestedPath) ||
      authorisedThroughRole(consumerName, requestedPath)

  fun matches(
    path: String,
    pathTemplate: String,
  ): Boolean =
    Regex(
      normalisePath(pathTemplate),
    ).matches(path)

  fun doesConsumerHaveIncludesAccess(
    consumerConfig: ConsumerConfig?,
    requestedPath: String,
  ): Boolean {
    consumerConfig?.permissions()?.forEach {
      if (matches(requestedPath, it)) {
        return true
      }
    }
    return false
  }

  fun doesConsumerHaveRoleAccess(
    consumerRolesInclude: List<String>,
    requestPath: String,
  ): Boolean {
    consumerRolesInclude.forEach {
      if (matches(requestPath, it)) {
        return true
      }
    }
    return false
  }

  private fun authorisedThroughRole(
    consumerName: String?,
    requestedPath: String,
  ): Boolean {
    val consumerConfig: ConsumerConfig? = consumers()[consumerName]
    val consumersRoles = consumerConfig?.roles
    val rolesInclude =
      buildList {
        for (consumerRole in consumersRoles.orEmpty()) {
          addAll(authorisationConfig.roles[consumerRole]?.permissions.orEmpty())
        }
      }
    val roleResult =
      doesConsumerHaveRoleAccess(rolesInclude, requestedPath)
    return roleResult
  }

  private fun authorisedThroughIncludes(
    consumerName: String?,
    requestedPath: String,
  ) = doesConsumerHaveIncludesAccess(consumers()[consumerName], requestedPath)

  /**
   * Returns true if the consumer has access to the endpoint.
   */
  fun hasAccess(
    consumerName: String,
    endpoint: String,
  ): Boolean = anyMatch(allPermissions(consumerName), endpoint)

  /**
   * Returns a list of consumers with access to a particular endpoint.
   */
  fun consumersWithAccess(endpoint: String): List<String> =
    authorisationConfig.consumers
      .filter { hasAccess(it.key, endpoint) }
      .map { it.key }
      .toList()
      .sorted()

  /**
   * Returns a list of all endpoint permissions for a consumer, whether direct or from roles.
   */
  fun allPermissions(consumerName: String): List<String> {
    val merged = mutableSetOf<String>()
    merged.addAll(authorisationConfig.consumers[consumerName]?.permissions().orEmpty())
    for (roleName in authorisationConfig.consumers[consumerName]?.roles ?: emptyList()) {
      merged.addAll(authorisationConfig.roles[roleName]?.permissions.orEmpty())
    }
    return merged.toList().sorted()
  }

  /**
   * Returns the integration events for a consumer.
   */
  fun events(consumerName: String): List<String> {
    val endpointMap = IntegrationEventType.entries.groupBy { normalisePath(it.pathTemplate) }
    return allPermissions(consumerName)
      .map { normalisePath(it) }
      .mapNotNull { endpointMap[it]?.map { eventType -> eventType.name } }
      .flatten()
      .ifEmpty { listOf("default") }
  }

  /**
   * Returns consumers with queues
   */
  fun consumersWithQueue(): Set<String> = authorisationConfig.consumers.filter { it.value?.queueName != null }.keys

  /**
   * Returns consumers without queues
   */
  fun consumersWithoutQueue(): Set<String> = authorisationConfig.consumers.filter { it.value?.queueName == null }.keys

  /**
   * Returns a consumers queue name (if applicable)
   */
  fun queueName(consumerName: String) = authorisationConfig.consumers[consumerName]?.queueName

  fun consumers() = authorisationConfig.consumers

  fun certificateRevocationList() = authorisationConfig.certificateRevocationList

  fun defaultConsumerName() = authorisationConfig.defaultConsumerName

  fun getRole(roleName: String) = authorisationConfig.roles[roleName]

  /**
   * Returns true if the endpoint matches any of the patterns.
   */
  private fun anyMatch(
    patterns: List<String>?,
    endpoint: String,
  ): Boolean = patterns != null && patterns.any { Regex(normalisePath(it)).matches(endpoint) }

  /**
   * Merges the filters from the consumer config and roles
   */
  fun allFilters(consumerName: String): ConsumerFilters? {
    val consumerConfig: ConsumerConfig? = authorisationConfig.consumers[consumerName]
    val roles: List<Role>? =
      consumerConfig?.roles?.mapNotNull {
        authorisationConfig.roles[it]
      }
    return allFilters(consumerConfig, roles)
  }

  fun allFilters(
    consumerConfig: ConsumerConfig?,
    roles: List<Role>?,
  ): ConsumerFilters? {
    val consumerPseudoRole = Role(permissions = null, filters = consumerConfig?.filters)
    val allRoles: List<Role> = listOf(consumerPseudoRole) + (roles ?: emptyList())

    if (allRoles.all { it.filters?.hasFilters() == false }) {
      return null
    }

    val supervisionStatuses =
      getDistinctValuesIfNotWildcarded(
        allRoles
          .filter { it.filters?.hasSupervisionStatusesFilter() == true }
          .mapNotNull { it.filters?.supervisionStatuses },
      )

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

    val alertCodes =
      getDistinctValuesIfNotWildcarded(
        allRoles
          .filter { it.filters?.hasAlertCodes() == true }
          .mapNotNull { it.filters?.alertCodes },
      )

    return if (allNull(caseNotes, prisons, mappaCategories, alertCodes, supervisionStatuses)) {
      ConsumerFilters.Companion.NO_FILTERS
    } else {
      ConsumerFilters(
        prisons,
        caseNotes,
        mappaCategories,
        alertCodes,
        supervisionStatuses,
      )
    }
  }

  fun allNull(vararg values: List<Any>?) = values.all { it == null }

  fun oboService(consumerName: String): OboService? = EntraJwtOboService()

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
