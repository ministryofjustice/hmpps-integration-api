package uk.gov.justice.digital.hmpps.hmppsintegrationapi.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.Name
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.FeatureNotEnabledException

/**
 * Feature flag configuration for the application.
 *
 * The values of the feature flags can be different in each environment, allowing
 * code to be enabled in a dev/test environment but not in production.
 *
 * Feature flag settings are in the `application-*.yml` files, with each having
 * a true/false value.
 */
@ConfigurationProperties()
data class FeatureFlagConfig(
  @Name("feature-flag")
  val flags: Map<String, Boolean> = mutableMapOf(),
) {
  companion object {
    // API feature flags
    const val COURSE_COMPLETION_EVENT = "course-completion-event-enabled"
    const val USE_ACTIVE_ALERTS_ENDPOINT = "use-active-alerts-endpoint"
    const val USE_CONTACT_EVENTS_ENDPOINT = "use-contact-events-endpoint"
    const val USE_EMERGENCY_CONTACTS_ENDPOINT = "use-emergency-contacts-endpoint"
    const val EPF_ENDPOINT_INCLUDES_LAO = "epf-endpoint-includes-lao"
    const val EPF_GATEWAY_DISABLED = "epf-gateway-disabled"
    const val CPR_ENABLED = "cpr-enabled"
    const val GATEWAY_CACHE_ENABLED = "gateway-cache-enabled"
    const val USE_WEBCLIENT_WRAPPER_FOR_HMPPS_AUTH = "use-webclient-wrapper-for-hmpps-auth"

    // Events feature flags
    const val ENABLE_DELETE_PROCESSED_EVENTS = "enable-delete-processed-events"
    const val ENABLE_PUBLISH_PENDING_EVENTS = "enable-publish-pending-events"
    const val ENABLE_DOMAIN_EVENTS_QUEUE_LISTENER = "enable-domain-events-queue-listener"
    const val ENABLE_SUBSCRIPTION_FILTER_POLICY_UPDATER = "enable-subscription-filter-policy-updater"
    const val PERSON_LANGUAGES_CHANGED_NOTIFICATIONS_ENABLED = "person-languages-changed-notifications-enabled"
    const val PRISONER_BASE_LOCATION_CHANGED_NOTIFICATIONS_ENABLED = "prisoner-base-location-changed-notifications-enabled"
    const val PRISONER_MERGED_NOTIFICATIONS_ENABLED = "prisoner-merge-notifications-enabled"
    const val CONTACT_EVENTS_NOTIFICATIONS_ENABLED = "contact-events-notifications-enabled"
    const val LIMITED_ACCESS_NOTIFICATIONS_ENABLED = "limited-access-notifications-enabled"
    const val DEDUPLICATE_EVENTS = "deduplicate-events"
  }

  /**
   * Returns the value of a feature flag, or null if it is not set.
   */
  fun getConfigFlagValue(feature: String): Boolean? = flags[feature]

  /**
   * Returns true if the feature flag is defined and set to true. Returns false by default
   */
  fun isEnabled(feature: String): Boolean = flags.getOrDefault(feature, false)

  /**
   * Returns true if the feature flag is defined and set to true. Returns true by default
   */
  fun isNotDisabled(feature: String): Boolean = flags.getOrDefault(feature, true)

  /**
   * Throws a [FeatureNotEnabledException] if the feature is not enabled.
   */
  fun require(feature: String) {
    if (!isEnabled(feature)) {
      throw FeatureNotEnabledException(feature)
    }
  }
}
