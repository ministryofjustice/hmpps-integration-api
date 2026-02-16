package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.events.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.AdditionalAnswers
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.EventNotification
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.IntegrationEventStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.services.IntegrationEventTopicService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.services.SendEventsService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase
import java.time.LocalDateTime

class StuckEventsIntegrationTest : IntegrationTestBase() {
  @Autowired
  private lateinit var sendEventsService: SendEventsService

  @MockitoBean
  private lateinit var integrationEventTopicService: IntegrationEventTopicService

  @BeforeEach
  fun setup() {
    whenever(integrationEventTopicService.sendEvent(any())).thenAnswer(
      AdditionalAnswers.answersWithDelay(
        300,
        { "SUCCESS" },
      ),
    )
    eventNotificationRepository.deleteAll()

    val baseDate = LocalDateTime.of(2025, 8, 12, 0, 0)
    eventNotificationRepository.save(makeEvent("MockUrl11", "claimId1", IntegrationEventStatus.PROCESSING.name, baseDate.plusDays(2).plusHours(1)))
    eventNotificationRepository.save(makeEvent("MockUrl12", "claimId1", IntegrationEventStatus.PENDING.name, baseDate.plusDays(1).plusHours(2)))
    eventNotificationRepository.save(makeEvent("MockUrl13", "claimId1", IntegrationEventStatus.PROCESSING.name, baseDate.plusDays(3).plusHours(3)))
    eventNotificationRepository.save(makeEvent("MockUrl14", "claimId2", IntegrationEventStatus.PROCESSING.name, baseDate))
    eventNotificationRepository.save(makeEvent("MockUrl15", "claimId2", IntegrationEventStatus.PROCESSING.name, baseDate.plusDays(2).plusHours(4)))
    eventNotificationRepository.save(makeEvent("MockUrl16", "claimId3", IntegrationEventStatus.PENDING.name, baseDate.plusDays(-1).plusHours(5)))
  }

  fun makeEvent(
    url: String,
    claimId: String? = null,
    status: String? = IntegrationEventStatus.PENDING.name,
    lastModifiedDateTime: LocalDateTime,
  ): EventNotification =
    EventNotification(
      eventType = "MAPPA_DETAIL_CHANGED",
      hmppsId = "MockId",
      prisonId = "MKI",
      url = url,
      claimId = claimId,
      status = status,
      lastModifiedDatetime = lastModifiedDateTime,
    )

  @Test
  fun `Stuck messages are found in the database`() {
    val expectedExceptionMessage =
      """
      stuck events with status PROCESSING
      """.trimIndent()
    val message = argumentCaptor<Throwable>()
    val thread1 = Thread { sendEventsService.sentNotifications() }
    thread1.start()
    verify(telemetryService, timeout(10_000).atLeast(1)).captureException(Throwable(message.capture()))
    assertThat(message.firstValue).hasMessageContaining(expectedExceptionMessage)
  }
}
