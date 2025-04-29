package uk.gov.justice.digital.hmpps.hmppsintegrationapi.config

import org.springframework.boot.context.properties.ConfigurationProperties
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.FeatureNotEnabledException

@ConfigurationProperties()
data class FeatureFlagConfig(
  val `feature-flag`: Map<String, Boolean> = mutableMapOf(),
) {
  companion object {
    const val USE_ARNS_ENDPOINTS = "use-arns-endpoints"
    const val USE_PHYSICAL_CHARACTERISTICS_ENDPOINTS = "use-physical-characteristics-endpoints"
    const val USE_IMAGE_ENDPOINTS = "use-image-endpoints"
    const val USE_EDUCATION_ASSESSMENTS_ENDPOINTS = "use-education-assessments-endpoints"
    const val USE_RESIDENTIAL_HIERARCHY_ENDPOINTS = "use-residential-hierarchy-endpoints"
    const val USE_RESIDENTIAL_DETAILS_ENDPOINTS = "use-residential-details-endpoints"
  }

  fun getConfigFlagValue(feature: String): Boolean? = `feature-flag`[feature]

  fun isEnabled(feature: String): Boolean = `feature-flag`.getOrDefault(feature, false)

  fun require(feature: String) {
    if (!isEnabled(feature)) {
      throw FeatureNotEnabledException(feature)
    }
  }
}
