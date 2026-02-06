package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.events.integration

import io.kotest.assertions.any
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.EventNotification
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.enums.IntegrationEventStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.services.DeleteProcessedEventsService
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.test.Test

class DeleteProcessedIntegrationTest : EventsIntegrationTestBase() {
  @Autowired
  private lateinit var deleteProcessedEventsService: DeleteProcessedEventsService

  @BeforeEach
  fun setup() {
    eventNotificationRepository.deleteAll()

    fun moreThan24HoursAgo() = LocalDateTime.now().minus(25, ChronoUnit.HOURS)

    fun lessThan24HoursAgo() = LocalDateTime.now().minus(3, ChronoUnit.HOURS)
    val events =
      listOf(
        EventNotification(status = IntegrationEventStatus.PROCESSED.name, lastModifiedDateTime = moreThan24HoursAgo(), claimId = "ID", hmppsId = "ID", eventType = "ID", prisonId = "ID", url = "URL"),
        EventNotification(status = IntegrationEventStatus.PROCESSED.name, lastModifiedDateTime = lessThan24HoursAgo(), claimId = "ID", hmppsId = "ID", eventType = "ID", prisonId = "ID", url = "URL"),
        EventNotification(status = IntegrationEventStatus.PROCESSED.name, lastModifiedDateTime = moreThan24HoursAgo(), claimId = "ID", hmppsId = "ID", eventType = "ID", prisonId = "ID", url = "URL"),
        EventNotification(status = IntegrationEventStatus.PROCESSED.name, lastModifiedDateTime = lessThan24HoursAgo(), claimId = "ID", hmppsId = "ID", eventType = "ID", prisonId = "ID", url = "URL"),
        EventNotification(status = IntegrationEventStatus.PENDING.name, lastModifiedDateTime = moreThan24HoursAgo(), claimId = "ID", hmppsId = "ID", eventType = "ID", prisonId = "ID", url = "URL"),
        EventNotification(status = IntegrationEventStatus.PROCESSED.name, lastModifiedDateTime = lessThan24HoursAgo(), claimId = "ID", hmppsId = "ID", eventType = "ID", prisonId = "ID", url = "URL"),
      )
    eventNotificationRepository.saveAll(events)
  }

  @Test
  fun `deletes processed events longer than 24 hours ago`() {
    val entriesBefore = eventNotificationRepository.count()
    deleteProcessedEventsService.deleteProcessedEvents()
    val entriesAfter = eventNotificationRepository.count()

    assertThat(entriesBefore).isEqualTo(6)
    assertThat(entriesAfter).isEqualTo(4)
  }

  @Test
  fun `delete throws exception - sends an exception to sentry and does not delete anything`() {
    doAnswer { Exception("Error deleting processed events") }.whenever(eventNotificationRepository).deleteEvents(any())
    val entriesBefore = eventNotificationRepository.count()
    deleteProcessedEventsService.deleteProcessedEvents()
    val entriesAfter =
      eventNotificationRepository
        .count()

    assertThat(entriesBefore).isEqualTo(6)
    assertThat(entriesAfter).isEqualTo(6)
  }
}
