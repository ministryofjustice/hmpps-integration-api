package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.service

import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.defaultObjectMapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.fixedClock
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.EventNotification
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.IntegrationEventStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.messaging.QueueService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.services.EventNotificationService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.AuthorisationService
import java.time.LocalDateTime

class EventNotificationServiceTest {
  private val fixedClock = fixedClock()

  @Test
  fun `suspended consumers should not receive notifications`() {
    val queueService = mock<QueueService>()
    val authService = mock<AuthorisationService>()
    val svc = EventNotificationService(queueService, defaultObjectMapper, authService, mock())

    whenever(authService.events(any())).thenReturn(listOf("MAPPA_DETAIL_CHANGED"))

    // Setup 2 consumers, one is suspended
    val consumers = setOf("suspended", "other")
    whenever(authService.consumersWithQueue()).thenReturn(consumers)
    whenever(authService.consumers()).thenReturn(
      mapOf(
        "suspended" to ConsumerConfig(isSuspended = true, queueName = "suspendedQ"),
        "other" to ConsumerConfig(isSuspended = false, queueName = "otherQ"),
      ),
    )

    // Send an event
    val event =
      EventNotification(
        eventId = 123,
        hmppsId = "A123456",
        eventType = "MAPPA_DETAIL_CHANGED",
        url = "mockUrl",
        status = IntegrationEventStatus.PENDING.name,
        lastModifiedDatetime = LocalDateTime.now(fixedClock),
      )
    svc.sendEvent(event)

    // Verify that only the non-suspended consumer is sent it
    verify(queueService, times(1)).sendMessageToQueue(any(), eq("otherQ"), isNull())
    verify(queueService, times(0)).sendMessageToQueue(any(), eq("suspendedQ"), isNull())
  }
}
