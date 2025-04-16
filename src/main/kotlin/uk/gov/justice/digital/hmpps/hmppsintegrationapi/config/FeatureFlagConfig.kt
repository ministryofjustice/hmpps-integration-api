package uk.gov.justice.digital.hmpps.hmppsintegrationapi.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "feature-flag")
data class FeatureFlagConfig(
  val useArnsEndpoints: Boolean,
  val useNumberOfChildrenEndpoints: Boolean,
  val usePrisonFilterImagesEndpoint: Boolean,
  val useVisibleCharacteristicsEndpoints: Boolean,
) {
  companion object {
    const val USE_ARNS_ENDPOINTS = "use-arns-endpoints"
    const val USE_NUMBER_OF_CHILDREN_ENDPOINTS = "use-number-of-children-endpoints"
    const val USE_PRISON_FILTER_IMAGES_ENDPOINT = "use-prison-filter-images-endpoint"
    const val USE_VISIBLE_CHARACTERISTICS_ENDPOINTS = "use-visible-characteristics-endpoints"
  }
}
