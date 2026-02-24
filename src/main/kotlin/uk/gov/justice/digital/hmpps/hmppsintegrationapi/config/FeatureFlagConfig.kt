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
    const val COURSE_COMPLETION_EVENT = "course-completion-event-enabled"
    const val USE_CONTACT_EVENTS_ENDPOINT = "use-contact-events-endpoint"
    const val EPF_ENDPOINT_INCLUDES_LAO = "epf-endpoint-includes-lao"
    const val EPF_GATEWAY_DISABLED = "epf-gateway-disabled"
    const val CPR_ENABLED = "cpr-enabled"
    const val GATEWAY_CACHE_ENABLED = "gateway-cache-enabled"
    const val NORMALISED_PATH_MATCHING = "normalised-path-matching"
    const val ENABLE_DELETE_PROCESSED_EVENTS = "enable-delete-processed-events"
    const val ENABLE_SEND_PROCESSED_EVENTS = "enable-send-processed-events"
    const val ENABLE_SEND_DECIMAL_RISK_SCORES = "enable-send-decimal-risk-scores"
  }

  /**
   * Returns the value of a feature flag, or null if it is not set.
   */
  fun getConfigFlagValue(feature: String): Boolean? = flags[feature]

  /**
   * Returns true if the  feature flag is defined and set to true.
   */
  fun isEnabled(feature: String): Boolean = flags.getOrDefault(feature, false)

  /**
   * Throws a [FeatureNotEnabledException] if the feature is not enabled.
   */
  fun require(feature: String) {
    if (!isEnabled(feature)) {
      throw FeatureNotEnabledException(feature)
    }
  }
}
