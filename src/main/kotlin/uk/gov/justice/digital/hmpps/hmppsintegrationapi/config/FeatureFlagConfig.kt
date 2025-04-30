package uk.gov.justice.digital.hmpps.hmppsintegrationapi.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "feature-flag")
data class FeatureFlagConfig(
  val useArnsEndpoints: Boolean,
  val usePhysicalCharacteristicsEndpoints: Boolean,
  val useImageEndpoints: Boolean,
  val useEducationAssessmentsEndpoints: Boolean,
  val useResidentialHierarchyEndpoints: Boolean,
  val useResidentialDetailsEndpoints: Boolean,
  val useCapacityEndpoint: Boolean,
) {
  companion object {
    const val USE_ARNS_ENDPOINTS = "use-arns-endpoints"
    const val USE_PHYSICAL_CHARACTERISTICS_ENDPOINTS = "use-physical-characteristics-endpoints"
    const val USE_IMAGE_ENDPOINTS = "use-image-endpoints"
    const val USE_EDUCATION_ASSESSMENTS_ENDPOINTS = "use-education-assessments-endpoints"
    const val USE_RESIDENTIAL_HIERARCHY_ENDPOINTS = "use-residential-hierarchy-endpoints"
    const val USE_RESIDENTIAL_DETAILS_ENDPOINTS = "use-residential-details-endpoints"
    const val USE_CAPACITY_ENDPOINT = "use-capacity-endpoint"
  }

  fun getConfigFlagValue(name: String): Boolean? =
    when (name) {
      USE_ARNS_ENDPOINTS -> this.useArnsEndpoints
      USE_RESIDENTIAL_HIERARCHY_ENDPOINTS -> this.useResidentialHierarchyEndpoints
      USE_RESIDENTIAL_DETAILS_ENDPOINTS -> this.useResidentialDetailsEndpoints
      USE_CAPACITY_ENDPOINT -> this.useCapacityEndpoint
      else -> null
    }
}
