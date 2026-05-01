package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.events.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.EventNotification
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.Metadata
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.enums.IntegrationEventType
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class EventNotificationRepositoryTest : IntegrationTestBase() {
  @BeforeEach
  fun setup() {
    eventNotificationRepository.deleteAll()
  }

  @Test
  fun `insert an event`() {
    val eventNotification =
      EventNotification(
        eventType = IntegrationEventType.MAPPA_DETAIL_CHANGED.name,
        hmppsId = "MockId",
        prisonId = "MKI",
        url = "MockUrl",
        lastModifiedDatetime = LocalDateTime.now().minusMinutes(6),
      )
    eventNotificationRepository.insert(eventNotification)

    val eventNotifications = eventNotificationRepository.findAll()
    assertThat(eventNotifications).hasSize(1)
    assertThat(eventNotifications[0].eventType).isEqualTo(eventNotification.eventType)
    assertThat(eventNotifications[0].hmppsId).isEqualTo(eventNotification.hmppsId)
    assertThat(eventNotifications[0].prisonId).isEqualTo(eventNotification.prisonId)
    assertThat(eventNotifications[0].url).isEqualTo(eventNotification.url)
    assertThat(eventNotifications[0].lastModifiedDatetime?.truncatedTo(ChronoUnit.MINUTES)).isEqualTo(eventNotification.lastModifiedDatetime?.truncatedTo(ChronoUnit.MINUTES))
  }

  @Test
  fun `on a conflict with a duplicate event, it does not update the timestamp on the existing record`() {
    val eventNotification =
      EventNotification(
        eventType = IntegrationEventType.MAPPA_DETAIL_CHANGED.name,
        hmppsId = "MockId",
        prisonId = "MKI",
        url = "MockUrl",
        metadata = Metadata(supervisionStatus = "PRISONS"),
        lastModifiedDatetime = LocalDateTime.now().minusMinutes(6),
      )
    eventNotificationRepository.insert(eventNotification)

    val eventNotifications = eventNotificationRepository.findAll()
    assertThat(eventNotifications).hasSize(1)

    val duplicatedEventNotification = eventNotification.copy(lastModifiedDatetime = LocalDateTime.now())
    eventNotificationRepository.insert(duplicatedEventNotification)

    val updatedEventNotifications = eventNotificationRepository.findAll()
    assertThat(updatedEventNotifications).hasSize(1)
    assertThat(updatedEventNotifications[0].lastModifiedDatetime?.truncatedTo(ChronoUnit.SECONDS)).isEqualTo(eventNotification.lastModifiedDatetime?.truncatedTo(ChronoUnit.SECONDS))
  }

  @Test
  fun `saves and returns the filters object with a supervision status`() {
    val eventNotification =
      EventNotification(
        eventType = IntegrationEventType.MAPPA_DETAIL_CHANGED.name,
        hmppsId = "MockId",
        prisonId = "MKI",
        url = "MockUrl",
        metadata = Metadata(supervisionStatus = "PROBATION"),
        lastModifiedDatetime = LocalDateTime.now().minusMinutes(6),
      )
    eventNotificationRepository.insert(eventNotification)
    val notifications = eventNotificationRepository.findAll()
    assertThat(notifications).hasSize(1)
    val notification = notifications[0]
    assertThat(notification.metadata).isEqualTo(Metadata(supervisionStatus = "PROBATION"))
  }

  @Test
  fun `saves and returns no filters object when filters is null`() {
    val eventNotification =
      EventNotification(
        eventType = IntegrationEventType.MAPPA_DETAIL_CHANGED.name,
        hmppsId = "MockId",
        prisonId = "MKI",
        url = "MockUrl",
        metadata = null,
        lastModifiedDatetime = LocalDateTime.now().minusMinutes(6),
      )
    eventNotificationRepository.insert(eventNotification)
    val notifications = eventNotificationRepository.findAll()
    assertThat(notifications).hasSize(1)
    val notification = notifications[0]
    assertThat(notification.metadata).isEqualTo(null)
  }
}
