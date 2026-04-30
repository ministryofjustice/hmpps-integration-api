package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.events.services

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.NullSource
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.bean.override.mockito.MockReset
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.EventNotification
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.IntegrationEventStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.Metadata
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.enums.IntegrationEventType
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.repository.JdbcTemplateEventNotificationRepository
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.services.EventNotificationService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.services.SendEventsService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestInMemoryQueueBase
import java.time.LocalDateTime
import java.util.UUID
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit

class IntegrationEventDirectSqsTest : IntegrationTestInMemoryQueueBase("testqueue") {
  @Autowired
  private lateinit var stateEventNotifierService: SendEventsService

  @Autowired
  private lateinit var eventRepository: JdbcTemplateEventNotificationRepository

  @MockitoSpyBean(reset = MockReset.BEFORE)
  private lateinit var eventNotificationService: EventNotificationService

  @BeforeEach
  fun purgeQueues() {
    Mockito.reset(eventNotificationService)
    testQueue.purge()
    eventRepository.deleteAll()
  }

  @AfterEach
  fun resetMocks() {
    Mockito.reset(authorisationConfig)
  }

  fun getEvent(
    prisonId: String? = null,
    url: String,
    metadata: Metadata? = null,
    eventType: String = IntegrationEventType.MAPPA_DETAIL_CHANGED.name,
  ) = EventNotification(
    status = IntegrationEventStatus.PENDING.name,
    eventType = eventType,
    hmppsId = "MockId",
    prisonId = prisonId,
    url = url,
    lastModifiedDatetime = LocalDateTime.now().minusMinutes(6),
    metadata = metadata,
    firstReceivedDatetime = LocalDateTime.now().minusMinutes(6),
  )

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = ["MKI"])
  @DisplayName("will publish Integration Event direct to consumer queue")
  @Throws(
    ExecutionException::class,
    InterruptedException::class,
  )
  fun willPublishDirectToQueue(prisonId: String?) {
    val event = getEvent(prisonId, UUID.randomUUID().toString())
    eventRepository.save(event)
    stateEventNotifierService.sentNotifications()

    await.atMost(5, TimeUnit.SECONDS).untilAsserted {
      Mockito.verify(eventNotificationService, Mockito.atLeast(1)).sendEvent(any())
      val prisonEventMessages = testQueueService.getQueue("testqueue").messagesAsObjects<EventNotification>()
      assertThat(prisonEventMessages).hasSize(1)
      assertThat(prisonEventMessages[0].eventType).isEqualTo(event.eventType)
    }
  }
}
