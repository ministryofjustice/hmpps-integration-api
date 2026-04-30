package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.events.services

import net.javacrumbs.jsonunit.assertj.JsonAssertions
import org.assertj.core.api.Assertions
import org.assertj.core.api.ThrowingConsumer
import org.awaitility.kotlin.await
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
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.EventNotification
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.IntegrationEventStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.enums.IntegrationEventType
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.repository.JdbcTemplateEventNotificationRepository
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.services.EventNotificationService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.services.SendEventsService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.events.IntegrationTestWithEventsQueueBase
import java.time.LocalDateTime
import java.util.UUID
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit

class IntegrationEventTest : IntegrationTestWithEventsQueueBase() {
  @Autowired
  private lateinit var stateEventNotifierService: SendEventsService

  @Autowired
  private lateinit var eventRepository: JdbcTemplateEventNotificationRepository

  @MockitoSpyBean(reset = MockReset.BEFORE)
  private lateinit var eventNotificationService: EventNotificationService

  @BeforeEach
  fun purgeQueues() {
    Mockito.reset(eventNotificationService)
    integrationEventTestQueueSqsClient.purgeQueue(PurgeQueueRequest.builder().queueUrl(integrationEventTestQueueUrl).build()).get()
  }

  fun getEvent(
    prisonId: String? = null,
    url: String,
  ) = EventNotification(
    status = IntegrationEventStatus.PENDING.name,
    eventType = IntegrationEventType.MAPPA_DETAIL_CHANGED.name,
    hmppsId = "MockId",
    prisonId = prisonId,
    url = url,
    lastModifiedDatetime = LocalDateTime.now().minusMinutes(6),
    firstReceivedDatetime = LocalDateTime.now().minusMinutes(6),
  )

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = ["MKI"])
  @DisplayName("will publish Integration Event with no prison Id")
  @Throws(
    ExecutionException::class,
    InterruptedException::class,
  )
  fun willPublishPrisonEvent(prisonId: String?) {
    await.atMost(5, TimeUnit.SECONDS).untilAsserted {
      eventRepository.save(getEvent(prisonId, UUID.randomUUID().toString()))
      stateEventNotifierService.sentNotifications()
      Mockito.verify(eventNotificationService, Mockito.atLeast(1)).sendEvent(any())
      val prisonEventMessages = getMessagesCurrentlyOnTestQueue()
      Assertions
        .assertThat(prisonEventMessages)
        .singleElement()
        .satisfies(
          ThrowingConsumer { event: String? ->
            JsonAssertions
              .assertThatJson(event)
              .node("eventType")
              .isEqualTo(IntegrationEventType.MAPPA_DETAIL_CHANGED.name)
            JsonAssertions
              .assertThatJson(event)
              .node("prisonId")
              .isEqualTo(prisonId)
          },
        )
    }
  }
}
