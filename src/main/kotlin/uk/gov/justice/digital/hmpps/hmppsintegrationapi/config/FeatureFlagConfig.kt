package uk.gov.justice.digital.hmpps.hmppsintegrationapi.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "feature-flag")
data class FeatureFlagConfig(
  val useArnsEndpoints: Boolean,
  val useNumberOfChildrenEndpoints: Boolean,
) {
  companion object {
    const val USE_ARNS_ENDPOINTS = "use-arns-endpoints"
    const val USE_NUMBER_OF_CHILDREN_ENDPOINTS = "use-number-of-children-endpoints"
  }

  fun fromDashSeparatedName(name: String): Boolean? =
    when (name) {
      USE_ARNS_ENDPOINTS -> this.useArnsEndpoints
      USE_NUMBER_OF_CHILDREN_ENDPOINTS -> this.useNumberOfChildrenEndpoints
      else -> null
    }
}
