package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerConfig

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
  val featureFlags: FeatureFlagConfig,
  val oboUserName: String? = null,
)
