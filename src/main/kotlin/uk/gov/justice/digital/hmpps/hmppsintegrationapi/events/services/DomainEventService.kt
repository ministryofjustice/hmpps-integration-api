package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.services

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.enums.IntegrationEventType
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.exceptions.UnmappableUrlException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.models.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.repository.EventNotificationRepository
import java.time.Clock
import java.time.LocalDateTime

@ConditionalOnProperty("feature-flag.${FeatureFlagConfig.ENABLE_DOMAIN_EVENTS_QUEUE_LISTENER}", havingValue = "true")
@Service
@Configuration
class DomainEventService(
  @Autowired val eventNotificationRepository: EventNotificationRepository,
  @Autowired val domainEventIdentitiesResolver: DomainEventIdentitiesResolver,
  @Value("\${services.int-api.base-url}") val baseUrl: String,
  private val clock: Clock,
  private val featureFlagConfig: FeatureFlagConfig,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  protected val log: Logger get() = Companion.log

  fun execute(hmppsDomainEvent: HmppsDomainEvent) {
    // Matching domain event to integration event type(s)
    val integrationEventTypes = filterEventTypes(hmppsDomainEvent)

    if (integrationEventTypes.isNotEmpty()) {
      val hmppsId = domainEventIdentitiesResolver.getHmppsId(hmppsDomainEvent)
      val prisonId = domainEventIdentitiesResolver.getPrisonId(hmppsDomainEvent)
      val additionalInformation = hmppsDomainEvent.additionalInformation

      for (integrationEventType in integrationEventTypes) {
        try {
          val currentTime = LocalDateTime.now(clock)
          val eventNotification = integrationEventType.getNotification(baseUrl, hmppsId, prisonId, additionalInformation, currentTime)

          eventNotificationRepository.insertOrUpdate(eventNotification)
        } catch (ume: UnmappableUrlException) {
          log.warn(ume.message)
        }
      }
    }
  }

  /**
   * Filter event [IntegrationEventType] from domain event [HmppsDomainEvent], configurable with feature-flag.
   *
   * Matching domain event to integration event type(s), respecting feature flags
   * - IntegrationEventTypes with no feature flag associated are enabled
   * - IntegrationEventTypes associated with a feature flag set to “true” are enabled
   * - IntegrationEventTypes associated with a feature flag set to “false” are not enabled
   * - IntegrationEventTypes that reference a feature flag that does not exist are disabled,
   *      * and an error is logged with the name of the event and the name of the flag
   */
  fun filterEventTypes(hmppsEvent: HmppsDomainEvent) =
    IntegrationEventType.entries
      .filter { isNotDisabled(it) }
      .filter { it.predicate.invoke(hmppsEvent) }

  private fun isNotDisabled(eventType: IntegrationEventType): Boolean {
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
