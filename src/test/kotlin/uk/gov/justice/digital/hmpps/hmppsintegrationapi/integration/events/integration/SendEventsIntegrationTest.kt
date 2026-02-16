package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.events.integration

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.AdditionalAnswers
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.EventNotification
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.IntegrationEventStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.services.DeleteProcessedEventsService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.services.IntegrationEventTopicService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.services.SendEventsService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase
import java.time.LocalDateTime

class SendEventsIntegrationTest : IntegrationTestBase() {
  @MockitoBean
  private lateinit var integrationEventTopicService: IntegrationEventTopicService

  @Autowired
  private lateinit var sendEventsService: SendEventsService

  @Autowired
  private lateinit var deleteProcessedService: DeleteProcessedEventsService

  @BeforeEach
  fun setup() {
    eventNotificationRepository.deleteAll()
    eventNotificationRepository.save(makeEvent("MockUrl1"))
    eventNotificationRepository.save(makeEvent("MockUrl2"))
    eventNotificationRepository.save(makeEvent("MockUrl3"))
    eventNotificationRepository.save(makeEvent("MockUrl4"))
    eventNotificationRepository.save(makeEvent("MockUrl5"))
  }

  fun makeEvent(url: String): EventNotification =
    EventNotification(
      eventType = "MAPPA_DETAIL_CHANGED",
      hmppsId = "MockId",
      prisonId = "MKI",
      url = url,
      lastModifiedDatetime = LocalDateTime.now().minusMinutes(7),
      eventId = null,
      claimId = null,
      status = IntegrationEventStatus.PENDING.name,
    )

  @Test
  fun `Concurrent Event Notifier services reads the DB records and deletes them without any exceptions`() {
    val thread1 = Thread { sendEventsService.sentNotifications() }
    val thread2 = Thread { sendEventsService.sentNotifications() }
    val deleteThread1 = Thread { deleteProcessedService.deleteProcessedEvents() }
    val deleteThread2 = Thread { deleteProcessedService.deleteProcessedEvents() }
    thread1.start()
    Thread.sleep(50)
    eventNotificationRepository.save(makeEvent("MockUrl6"))
    eventNotificationRepository.save(makeEvent("MockUrl7"))
    eventNotificationRepository.save(makeEvent("MockUrl8"))
    eventNotificationRepository.save(makeEvent("MockUrl9"))
    eventNotificationRepository.save(makeEvent("MockUrl10"))
    thread2.start()
    deleteThread1.start()
    deleteThread2.start()
    // Await until all are processed
    Awaitility.await().until { eventNotificationRepository.findAll().map { it.status }.toSet() == setOf("PROCESSED") }
    verify(telemetryService, never()).captureException(any())
  }

  @Test
  fun `Concurrent Event Notifier services reads the DB records and processes them with exceptions`() {
    // Run a claim with exceptions
    whenever(integrationEventTopicService.sendEvent(argThat<EventNotification> { url == "MockUrl2" || url == "MockUrl4" })).thenThrow(
      RuntimeException("Some AWS exception"),
    )
    sendEventsService.sentNotifications()

    // Run a claim without exceptions
    whenever(integrationEventTopicService.sendEvent(any())).thenAnswer(
      AdditionalAnswers.answersWithDelay(
        300,
      ) { "SUCCESS" },
    )
    sendEventsService.sentNotifications()
    // Check all are processed
    assertThat(eventNotificationRepository.findAll().map { it.status }.toSet()).isEqualTo(setOf("PROCESSED"))
    verify(telemetryService, times(2)).captureException(any())
  }
}
