package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.config

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig

data class FeatureFlagTestConfig(
  private val featureFlags: MutableMap<String, Boolean> = mutableMapOf(),
  val featureFlagConfig: FeatureFlagConfig = FeatureFlagConfig(featureFlags),
) {
  fun assumeFeatureFlag(
    feature: String,
    enabled: Boolean? = null,
  ) {
    if (enabled != null) {
      featureFlags[feature] = enabled
    } else {
      resetFeatureFlag(feature)
    }
  }

  fun resetFeatureFlag(feature: String) {
    featureFlags.remove(feature)
  }
}
