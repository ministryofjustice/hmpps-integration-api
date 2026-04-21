package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.service

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.fixedClock
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.EventNotification
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.IntegrationEventStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.repository.JdbcTemplateEventNotificationRepository
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.services.EventNotificationService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.services.SendEventsService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.dsl.objectMapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.telemetry.TelemetryService
import java.time.LocalDateTime

class SendEventServiceTest {
  private lateinit var sendEventsService: SendEventsService

  private val eventNotificationService: EventNotificationService = mock()
  private val eventRepository: JdbcTemplateEventNotificationRepository = mock()
  private val telemetryService: TelemetryService = mock()
  private val fixedClock = fixedClock()

  @BeforeEach
  fun setUp() {
    Mockito.reset(eventRepository)

    sendEventsService = SendEventsService(eventNotificationService, eventRepository, telemetryService, fixedClock)
  }

  @Test
  fun `No event published when repository return no event notifications`() {
    whenever(eventRepository.findAllWithLastModifiedDateTimeBefore(any())).thenReturn(emptyList())
    sendEventsService.sentNotifications()
    verifyNoInteractions(eventNotificationService)
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
        status = IntegrationEventStatus.PENDING.name,
        lastModifiedDatetime = LocalDateTime.now(fixedClock),
      )
    whenever(eventRepository.findAllProcessingEvents(any())).thenReturn(listOf(event))

    sendEventsService.sentNotifications()

    argumentCaptor<EventNotification>().apply {
      verify(eventNotificationService, times(1)).sendEvent(capture())
      Assertions.assertThat(firstValue).isEqualTo(event)
    }
  }

  @Test
  fun `Event should be published with the correct fields`() {
    val lastModifiedDateTime = LocalDateTime.of(2021, 1, 1, 1, 0, 2)
    val event =
      EventNotification(
        eventId = 123,
        hmppsId = "hmppsId",
        eventType = "MAPPA_DETAIL_CHANGED",
        prisonId = "MKI",
        url = "mockUrl",
        status = IntegrationEventStatus.PENDING.name,
        lastModifiedDatetime = lastModifiedDateTime,
      )
    val jsonObject = objectMapper.writeValueAsString(event)
    val expected =
      """
      {
        "eventId" : 123,
        "hmppsId" : "hmppsId",
        "eventType" : "MAPPA_DETAIL_CHANGED",
        "prisonId" : "MKI",
        "url" : "mockUrl",
        "lastModifiedDateTime" : "$lastModifiedDateTime"
      }
    """.removeWhitespaceAndNewlines().trimIndent()

    assertThat(jsonObject).isEqualTo(expected)
  }

  @Test
  fun `on sns publish error do not delete event from db`() {
    val event =
      EventNotification(
        eventId = 123,
        hmppsId = "hmppsId",
        eventType = "MAPPA_DETAIL_CHANGED",
        url = "mockUrl",
        status = IntegrationEventStatus.PENDING.name,
        lastModifiedDatetime = LocalDateTime.now(fixedClock),
      )
    whenever(eventRepository.findAllWithLastModifiedDateTimeBefore(any())).thenReturn(listOf(event))
    whenever(eventNotificationService.sendEvent(event)).thenThrow(RuntimeException("error"))
    sendEventsService.sentNotifications()
    verify(eventRepository, times(0)).deleteById(123)
  }
}
