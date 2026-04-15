package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.services.domain

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.Metadata
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.exceptions.UnmappableUrlException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.models.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.repository.EventNotificationRepository
import java.time.Clock
import java.time.LocalDateTime

@ConditionalOnProperty("feature-flag.${FeatureFlagConfig.DEDUPLICATE_EVENTS}", havingValue = "true")
@Service
class DeduplicationDomainEventService(
  @Autowired val eventNotificationRepository: EventNotificationRepository,
  @Autowired val domainEventIdentitiesResolver: DomainEventIdentitiesResolver,
  @Value("\${services.int-api.base-url}") val baseUrl: String,
  private val clock: Clock,
  private val featureFlagConfig: FeatureFlagConfig,
) : DomainEventService {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  protected val log: Logger get() = Companion.log

  override fun execute(hmppsDomainEvent: HmppsDomainEvent) {
    // Matching domain event to integration event type(s)
    val integrationEventTypes = filterEventTypes(hmppsDomainEvent, featureFlagConfig)

    if (integrationEventTypes.isNotEmpty()) {
      val hmppsId = domainEventIdentitiesResolver.getHmppsId(hmppsDomainEvent)
      val prisonId = domainEventIdentitiesResolver.getPrisonId(hmppsDomainEvent)
      val supervisionStatus = domainEventIdentitiesResolver.getSupervisionStatus(hmppsId)
      val additionalInformation = hmppsDomainEvent.additionalInformation

      for (integrationEventType in integrationEventTypes) {
        try {
          val currentTime = LocalDateTime.now(clock)
          val eventNotification =
            integrationEventType.getNotification(
              baseUrl,
              hmppsId,
              prisonId,
              additionalInformation,
              currentTime,
              supervisionStatus?.let { Metadata(supervisionStatus = supervisionStatus) },
            )

          eventNotificationRepository.insertOrUpdate(eventNotification)
        } catch (ume: UnmappableUrlException) {
          log.warn(ume.message)
        }
      }
    }
  }
}
