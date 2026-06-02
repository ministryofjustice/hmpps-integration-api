package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

/**
 * Context information for an API request.
 *
 * This includes the consymer ID and the configuration attached to that consumer,
 * as well as the feature flag configuration for the request.
 *
 * Accessing the feature flags through the request context allows for feature
 * flag values to be overridden on a per-request basis in some situations.
 */
data class RequestContext(
  val consumerId: String,
  val consumerConfig: ConsumerConfig,
  val filters: ConsumerFilters?,
  val featureFlags: FeatureFlagConfig,
  val oboUserName: String? = null,
) {
  companion object {
    fun buildRequestContext(
      consumerId: String? = null,
      consumerConfig: ConsumerConfig? = null,
      filters: ConsumerFilters? = null,
      featureFlags: FeatureFlagConfig? = null,
      oboUserName: String? = null,
    ): RequestContext =
      RequestContext(
        consumerId = consumerId ?: "",
        consumerConfig = consumerConfig ?: ConsumerConfig(),
        filters = filters,
        featureFlags = featureFlags ?: FeatureFlagConfig(),
        oboUserName = oboUserName,
      )
  }
}
