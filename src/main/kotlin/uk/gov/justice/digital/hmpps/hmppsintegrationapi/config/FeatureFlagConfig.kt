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
    const val USE_ARNS_ENDPOINTS = "use-arns-endpoints"
    const val USE_EDUCATION_ASSESSMENTS_ENDPOINTS = "use-education-assessments-endpoints"
    const val USE_ESWE_CURIOUS_ENDPOINTS = "use-eswe-curious-endpoints"
    const val USE_UPDATE_ATTENDANCE_ENDPOINT = "use-update-attendance-endpoint"
    const val USE_SCHEDULE_DETAIL_ENDPOINT = "use-schedule-detail-endpoint"
    const val USE_SEARCH_APPOINTMENTS_ENDPOINT = "use-search-appointments-endpoint"
    const val USE_DEALLOCATION_ENDPOINT = "use-deallocation-endpoint"
    const val USE_DEALLOCATION_REASONS_ENDPOINT = "use-deallocation-reasons-endpoint"
    const val USE_ALLOCATION_ENDPOINT = "use-allocation-endpoint"
    const val USE_SUITABILITY_ENDPOINT = "use-suitability-endpoint"
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
