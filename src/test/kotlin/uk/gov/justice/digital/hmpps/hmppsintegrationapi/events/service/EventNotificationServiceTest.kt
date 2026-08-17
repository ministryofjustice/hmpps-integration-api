package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.service

import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.defaultObjectMapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.fixedClock
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.EventNotification
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.IntegrationEventStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.models.DirectSQSMessage
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.models.EventType
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.models.SQSMessageAttributes
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.services.EventNotificationService
import java.time.LocalDateTime

class EventNotificationServiceTest {
  private val fixedClock = fixedClock()

  @Test
  fun `log notification for suspended consumer`() {
    val svc = EventNotificationService(mock(), mock(), mock(), mock())
    val event =
      EventNotification(
        eventId = 123,
        hmppsId = "A123456",
        eventType = "MAPPA_DETAIL_CHANGED",
        url = "mockUrl",
        status = IntegrationEventStatus.PENDING.name,
        lastModifiedDatetime = LocalDateTime.now(fixedClock),
      )
    val message =
      DirectSQSMessage(
        message = defaultObjectMapper.writeValueAsString(event),
        messageAttributes = SQSMessageAttributes(EventType(event.eventType)),
      )
    val messageText = defaultObjectMapper.writeValueAsString(message)

    svc.logSuspendedConsumerNotification(messageText, "testqueue")
  }
}
