package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.events.service

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.EventNotification
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.IntegrationEventStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.repository.EventNotificationRepository
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.services.IntegrationEventTopicService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.services.SendEventsService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.telemetry.TelemetryService
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

@ActiveProfiles("test")
@JsonTest
class SendEventServiceTest {
  private lateinit var eventNotifierService: SendEventsService

  private val integrationEventTopicService: IntegrationEventTopicService = mock()
  private val eventRepository: EventNotificationRepository = mock()
  private val telemetryService: TelemetryService = mock()
  private val currentTime: LocalDateTime = LocalDateTime.now()
  private val zonedCurrentTime: ZonedDateTime = currentTime.atZone(ZoneId.systemDefault())
  private val testClock: Clock = Clock.fixed(zonedCurrentTime.toInstant(), zonedCurrentTime.zone)

  @BeforeEach
  fun setUp() {
    Mockito.reset(eventRepository)

    eventNotifierService = SendEventsService(integrationEventTopicService, eventRepository, telemetryService, testClock)
  }

  @Test
  fun `No event published when repository return no event notifications`() {
    whenever(eventRepository.findAllWithLastModifiedDateTimeBefore(any())).thenReturn(emptyList())
    eventNotifierService.sentNotifications()
    verifyNoInteractions(integrationEventTopicService)
  }

  @Test
  fun `Event published for event notification in database`() {
    val event =
      EventNotification(
        eventId = 123,
        hmppsId = "hmppsId",
        eventType = "MAPPA_DETAIL_CHANGED",
        prisonId = "MKI",
        url = "mockUrl",
        lastModifiedDatetime = currentTime,
        claimId = null,
        status = IntegrationEventStatus.PENDING.name,
      )
    whenever(eventRepository.findAllProcessingEvents(any())).thenReturn(listOf(event))

    eventNotifierService.sentNotifications()

    argumentCaptor<EventNotification>().apply {
      verify(integrationEventTopicService, times(1)).sendEvent(capture())
      Assertions.assertThat(firstValue).isEqualTo(event)
    }
  }

  @Test
  fun `on sns publish error do not delete event from db`() {
    val event =
      EventNotification(
        eventId = 123,
        hmppsId = "hmppsId",
        eventType = "MAPPA_DETAIL_CHANGED",
        prisonId = null,
        url = "mockUrl",
        lastModifiedDatetime = currentTime,
        claimId = null,
        status = IntegrationEventStatus.PENDING.name,
      )
    whenever(eventRepository.findAllWithLastModifiedDateTimeBefore(any())).thenReturn(listOf(event))
    whenever(integrationEventTopicService.sendEvent(event)).thenThrow(RuntimeException("error"))
    eventNotifierService.sentNotifications()
    verify(eventRepository, times(0)).deleteById(123)
  }
}
