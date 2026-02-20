package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.services

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.repository.EventNotificationRepository
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.telemetry.TelemetryService
import java.time.Clock
import java.time.LocalDateTime
import java.util.UUID

@ConditionalOnProperty("feature-flag.${FeatureFlagConfig.ENABLE_SEND_PROCESSED_EVENTS}", havingValue = "true")
@Component
@Configuration
class SendEventsService(
  private val integrationEventTopicService: IntegrationEventTopicService,
  private val eventRepository: EventNotificationRepository,
  private val telemetryService: TelemetryService,
  private val clock: Clock,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @Scheduled(fixedRateString = "\${notifier.schedule.rate}")
  fun sentNotifications() {
    alertForAnyStuckMessages()
    val fiveMinutesAgo = LocalDateTime.now(clock).minusMinutes(5)

    val claimId = UUID.randomUUID().toString()

    log.info("Setting to processing with claim id $claimId")
    // Claim records to process
    val eventsSetToProcessing = eventRepository.setProcessing(fiveMinutesAgo, claimId)

    log.info("Set $eventsSetToProcessing to processing with claim id $claimId")

    val events = eventRepository.findAllProcessingEvents(claimId)

    log.info("Sending ${events.size} events for claim id $claimId")
    var sentEvents = 0
    events.forEach {
      try {
        integrationEventTopicService.sendEvent(it)
        eventRepository.setProcessed(it.eventId!!)
        sentEvents++
      } catch (e: Exception) {
        log.error("Error caught with msg ${e.message} for claim id $claimId", e)
        telemetryService.captureException(e)
        // If we encounter any exceptions then reset the event record to pending so it can be retried by another claim
        log.info("Reset failed event back to PENDING with claim id ${it.claimId}")
        eventRepository.setPending(it.eventId!!)
      }
    }
    log.info("Successfully sent $sentEvents out of ${events.size} events for claim id $claimId")
  }

  private fun alertForAnyStuckMessages() {
    val stuck = eventRepository.getStuckEvents(LocalDateTime.now(clock).minusMinutes(10))
    if (stuck.isNotEmpty()) {
      val messages =
        stuck.map {
          "${it.eventCount} stuck events with status ${it.status}. Earliest event has date ${it.earliestDatetime}"
        }
      telemetryService.captureException(Throwable(messages.joinToString("\n")))
    }
  }
}
