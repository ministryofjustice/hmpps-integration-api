package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.events.services

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.EventNotification
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.IntegrationEventStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.services.DeleteProcessedEventsService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.test.Test

class DeleteProcessedIntegrationTest : IntegrationTestBase() {
  @Autowired
  private lateinit var deleteProcessedEventsService: DeleteProcessedEventsService

  @BeforeEach
  fun setup() {
    eventNotificationRepository.deleteAll()

    fun moreThan24HoursAgo() = LocalDateTime.now().minus(25, ChronoUnit.HOURS)

    fun lessThan24HoursAgo() = LocalDateTime.now().minus(3, ChronoUnit.HOURS)
    val events =
      listOf(
        EventNotification(
          claimId = "ID",
          hmppsId = "ID",
          eventType = "ID",
          prisonId = "ID",
          url = "URL",
          status = IntegrationEventStatus.PROCESSED.name,
          lastModifiedDatetime = moreThan24HoursAgo(),
          firstReceivedDatetime = moreThan24HoursAgo(),
        ),
        EventNotification(
          claimId = "ID",
          hmppsId = "ID",
          eventType = "ID",
          prisonId = "ID",
          url = "URL",
          status = IntegrationEventStatus.PROCESSED.name,
          lastModifiedDatetime = lessThan24HoursAgo(),
          firstReceivedDatetime = lessThan24HoursAgo(),
        ),
        EventNotification(
          claimId = "ID",
          hmppsId = "ID",
          eventType = "ID",
          prisonId = "ID",
          url = "URL",
          status = IntegrationEventStatus.PROCESSED.name,
          lastModifiedDatetime = moreThan24HoursAgo(),
          firstReceivedDatetime = moreThan24HoursAgo(),
        ),
        EventNotification(
          claimId = "ID",
          hmppsId = "ID",
          eventType = "ID",
          prisonId = "ID",
          url = "URL",
          status = IntegrationEventStatus.PROCESSED.name,
          lastModifiedDatetime = lessThan24HoursAgo(),
          firstReceivedDatetime = lessThan24HoursAgo(),
        ),
        EventNotification(
          claimId = "ID",
          hmppsId = "ID",
          eventType = "ID",
          prisonId = "ID",
          url = "URL",
          status = IntegrationEventStatus.PENDING.name,
          lastModifiedDatetime = moreThan24HoursAgo(),
          firstReceivedDatetime = moreThan24HoursAgo(),
        ),
        EventNotification(
          claimId = "ID",
          hmppsId = "ID",
          eventType = "ID",
          prisonId = "ID",
          url = "URL",
          status = IntegrationEventStatus.PROCESSED.name,
          lastModifiedDatetime = lessThan24HoursAgo(),
          firstReceivedDatetime = lessThan24HoursAgo(),
        ),
      )
    eventNotificationRepository.saveAll(events)
  }

  @Test
  fun `deletes processed events longer than 24 hours ago`() {
    val entriesBefore = eventNotificationRepository.count()
    deleteProcessedEventsService.deleteProcessedEvents()
    val entriesAfter = eventNotificationRepository.count()

    Assertions.assertThat(entriesBefore).isEqualTo(6)
    Assertions.assertThat(entriesAfter).isEqualTo(4)
  }

  @Test
  fun `delete throws exception - sends an exception to sentry and does not delete anything`() {
    doAnswer { Exception("Error deleting processed events") }.whenever(eventNotificationRepository).deleteEvents(any())
    val entriesBefore = eventNotificationRepository.count()
    deleteProcessedEventsService.deleteProcessedEvents()
    val entriesAfter =
      eventNotificationRepository
        .count()

    Assertions.assertThat(entriesBefore).isEqualTo(6)
    Assertions.assertThat(entriesAfter).isEqualTo(6)
  }
}
