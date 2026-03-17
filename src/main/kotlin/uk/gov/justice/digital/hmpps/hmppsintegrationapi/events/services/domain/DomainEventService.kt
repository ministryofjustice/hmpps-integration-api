package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.services.domain

import org.slf4j.LoggerFactory
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.enums.IntegrationEventType
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.models.HmppsDomainEvent

interface DomainEventService {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun execute(hmppsDomainEvent: HmppsDomainEvent)

  /**
   * Filter event [uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.enums.IntegrationEventType] from domain event [HmppsDomainEvent], configurable with feature-flag.
   *
   * Matching domain event to integration event type(s), respecting feature flags
   * - IntegrationEventTypes with no feature flag associated are enabled
   * - IntegrationEventTypes associated with a feature flag set to “true” are enabled
   * - IntegrationEventTypes associated with a feature flag set to “false” are not enabled
   * - IntegrationEventTypes that reference a feature flag that does not exist are disabled,
   *      * and an error is logged with the name of the event and the name of the flag
   */
  fun filterEventTypes(
    hmppsEvent: HmppsDomainEvent,
    featureFlagConfig: FeatureFlagConfig,
  ) = IntegrationEventType.entries
    .filter { isNotDisabled(it, featureFlagConfig) }
    .filter { it.predicate.invoke(hmppsEvent) }

  fun isNotDisabled(
    eventType: IntegrationEventType,
    featureFlagConfig: FeatureFlagConfig,
  ): Boolean {
    // Filter event types per feature flag, if associated with
    return eventType.featureFlag?.let { feature ->
      // i) enabled or disabled according to the defined feature flag;
      // ii) otherwise disabled, when feature flag is associated but undefined
      featureFlagConfig.getConfigFlagValue(feature) ?: run {
        log.error("Missing feature flag \"{}\" of event type \"{}\"", feature, eventType.name)
        false
      }
    } ?: true // default true (enabled), if no feature-flag has been associated
  }
}
