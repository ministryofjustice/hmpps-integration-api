package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.events.services

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.EventNotification
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.IntegrationEventStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.enums.IntegrationEventType
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase
import java.time.LocalDateTime
import java.util.UUID

class DatabaseConstraintTest : IntegrationTestBase() {
  fun makeEvent(url: String): EventNotification =
    EventNotification(
      eventType = IntegrationEventType.MAPPA_DETAIL_CHANGED.name,
      hmppsId = "MockId",
      prisonId = "MKI",
      url = url,
      lastModifiedDatetime = LocalDateTime.now().minusHours(25),
    )

  @BeforeEach
  fun setUp() {
    eventNotificationRepository.deleteAll()
  }

  @Test
  fun `does not create a new record when url, event type and status are all the same and all records are pending`() {
    Assertions.assertThat(eventNotificationRepository.count()).isEqualTo(0)
    eventNotificationRepository.insert(makeEvent("MockUrl1"))
    Assertions.assertThat(eventNotificationRepository.count()).isEqualTo(1)
    eventNotificationRepository.insert(makeEvent("MockUrl1"))
    Assertions.assertThat(eventNotificationRepository.count()).isEqualTo(1)
  }

  @Test
  fun `creates a new record when url, event type are same, but status is different`() {
    Assertions.assertThat(eventNotificationRepository.count()).isEqualTo(0)
    val claimId = UUID.randomUUID().toString()
    eventNotificationRepository.insert(makeEvent("MockUrl1"))
    Assertions.assertThat(eventNotificationRepository.count()).isEqualTo(1)
    // Move status to processing
    eventNotificationRepository.setProcessing(LocalDateTime.now().minusMinutes(5), claimId)
    eventNotificationRepository.insert(makeEvent("MockUrl1"))
    Assertions.assertThat(eventNotificationRepository.count()).isEqualTo(2)
  }

  @Test
  fun `allows another record with the same url and event type to be set to PROCESSING`() {
    // Put an event in the processing state
    val claimId1 = UUID.randomUUID().toString()
    eventNotificationRepository.insert(makeEvent("MockUrl1"))
    eventNotificationRepository.setProcessing(LocalDateTime.now().minusMinutes(5), claimId1)

    // Put an event in the processing state
    val claimId2 = UUID.randomUUID().toString()
    eventNotificationRepository.insert(makeEvent("MockUrl1"))
    eventNotificationRepository.setProcessing(LocalDateTime.now().minusMinutes(5), claimId2)

    val events = eventNotificationRepository.findAll()
    Assertions.assertThat(events).hasSize(2)
    Assertions.assertThat(events[0].status).isEqualTo(IntegrationEventStatus.PROCESSING.name)
    Assertions.assertThat(events[1].status).isEqualTo(IntegrationEventStatus.PROCESSING.name)
  }
}
