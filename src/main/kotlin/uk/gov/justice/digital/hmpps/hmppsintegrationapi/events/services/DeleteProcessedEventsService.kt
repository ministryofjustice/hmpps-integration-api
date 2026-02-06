package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.services

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.repository.EventNotificationRepository
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.telemetry.TelemetryService
import java.time.Clock
import java.time.LocalDateTime

@ConditionalOnProperty("feature-flag.enable-delete-processed-events", havingValue = "true")
@Component
class DeleteProcessedEventsService(
  val eventRepository: EventNotificationRepository,
  private val telemetryService: TelemetryService,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @Scheduled(fixedRateString = "\${delete-processed-events.schedule.rate}")
  @Transactional
  fun deleteProcessedEvents() {
    val cutOff = LocalDateTime.now(Clock.systemDefaultZone()).minusHours(24)
    try {
      log.info("Deleting processed events older than $cutOff")
      eventRepository.deleteEvents(cutOff)
    } catch (e: Exception) {
      log.error("Error deleting processed events", e)
      telemetryService.captureException(e)
    }
    log.info("Successfully deleted processed events")
  }
}
