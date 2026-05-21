package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.AuthorisationConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.fixedClock
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.enums.IntegrationEventType
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.normalisePath
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig.RedactionPolicy
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.Role
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.MappaCategory
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.onbehalfof.OboService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.onbehalfof.UnsignedJwtOboService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.telemetry.TelemetryService
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.collections.orEmpty
import kotlin.math.ceil

@Component
class AuthorisationService(
  private val authorisationConfig: AuthorisationConfig,
  private val telemetryService: TelemetryService,
  private val clock: Clock = fixedClock(),
) {
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

  fun redactionPolicies(consumerName: String): List<RedactionPolicy> =
    authorisationConfig.consumers[consumerName]?.roles?.flatMap {
      authorisationConfig.roles[it]?.redactionPolicies ?: emptyList()
    } ?: emptyList()

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

  fun oboService(consumerName: String): OboService? {
    val oboServiceName = authorisationConfig.consumers[consumerName]?.oboConfig?.strategy
    return when (oboServiceName) {
      "unsigned" -> UnsignedJwtOboService()
      "entra" -> null // EntraJwtOboService()
      else -> null
    }
  }

  fun requiresObo(consumerName: String): Boolean = authorisationConfig.consumers[consumerName]?.oboConfig != null

  /**
   * Converts a certificate expiry date in the OpenSSL format to an ISO-6801 format
   * Creates a sentry alert if the number of days to expiry is in the range 30, 21, 14, 7..0
   * If the date is not in the OpenSSL format, will capture exception and return null
   * Throws a RuntimeException if the certificate has already expired
   *
   * @param certExpiryDate The certificate expiry date in the OpenSSL format e.g Jun 7 12:30:10 2026 GMT
   * @param consumerName The consumer name
   * @return The certificate expiry date in ISO-8601 format
   */

  fun processCertificateExpiryDate(
    certExpiryDate: String,
    consumerName: String,
  ): String? {
    val expiryDateTime =
      try {
        ZonedDateTime
          .parse(certExpiryDate, DateTimeFormatter.ofPattern("MMM d HH:mm:ss yyyy zzz", Locale.ENGLISH))
          .toInstant()
      } catch (ex: Exception) {
        telemetryService.captureException(RuntimeException("Failed to parse certificate expiry date $certExpiryDate. ${ex.message}"))
        null
      }
    return expiryDateTime?.let {
      checkExpiryDate(expiryDateTime, certExpiryDate, consumerName)
      expiryDateTime.toString()
    }
  }

  fun checkExpiryDate(
    expiryDateTime: Instant,
    certExpiryDateString: String,
    consumerName: String,
  ) {
    val today = LocalDate.ofInstant(clock.instant(), clock.zone)
    val expires = LocalDate.ofInstant(expiryDateTime, clock.zone)
    val days = ChronoUnit.DAYS.between(today, expires)

    val expiryWarningMessage = expiryWarningMessage(days, certExpiryDateString, consumerName)

    if (expiryWarningMessage != null) {
      telemetryService.captureMessage(expiryWarningMessage)
    }
  }

  fun expiryWarningMessage(
    days: Long,
    expiryDateTime: String,
    consumerName: String,
  ): String? {
    val durationMessage =
      when {
        (days < 0) -> throw RuntimeException("The certificate for $consumerName with expiry date $expiryDateTime has expired")
        (days <= 7) -> "in $days ${if (days == 1L) "day" else "days"}"
        (days <= 28) -> {
          val weeks = ceil(days / 7.0).toInt()
          "in under $weeks ${if (weeks == 1) "week" else "weeks"}"
        }
        (days <= 30) -> "in under 30 days"
        else -> null
      }
    return durationMessage?.let {
      "The certificate for $consumerName will expire $durationMessage ($expiryDateTime)"
    }
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
