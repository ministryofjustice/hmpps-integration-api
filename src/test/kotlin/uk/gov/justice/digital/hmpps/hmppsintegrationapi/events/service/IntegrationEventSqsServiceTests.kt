package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.service

import io.kotest.matchers.string.shouldContain
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import software.amazon.awssdk.services.sqs.model.SendMessageResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.AuthorisationConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.ConfigTest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.EventNotification
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.IntegrationEventStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.models.DirectSQSMessage
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.services.EventNotificationService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.MockMvcExtensions.objectMapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.telemetry.TelemetryService
import uk.gov.justice.hmpps.sqs.HmppsQueue
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture

class IntegrationEventSqsServiceTests : ConfigTest() {
  val hmppsQueueService: HmppsQueueService = mock()
  val sqsClient: SqsAsyncClient = mock()
  val mockQueue: HmppsQueue = mock()
  val authorisationConfig: AuthorisationConfig = mock()
  val featureFlagConfig: FeatureFlagConfig = mock()
  val telemetryService: TelemetryService = mock()

  private lateinit var eventNotificationService: EventNotificationService
  val currentTime: LocalDateTime = LocalDateTime.now()

  val event =
    EventNotification(
      eventId = 123,
      hmppsId = "hmppsId",
      eventType = "MAPPA_DETAIL_CHANGED",
      prisonId = "MKI",
      url = "mockUrl",
      status = IntegrationEventStatus.PROCESSING.name,
      lastModifiedDatetime = currentTime,
      claimId = null,
    )

  val response: SendMessageResponse? =
    SendMessageResponse
      .builder()
      .messageId("123")
      .build()

  @BeforeEach
  fun setUp() {
    whenever(featureFlagConfig.isEnabled(FeatureFlagConfig.DIRECT_SQS_NOTIFICATIONS)).thenReturn(true)
    whenever(authorisationConfig.consumersWithQueue()).thenReturn(setOf("mockConsumer1", "mockConsumer2"))
    whenever(authorisationConfig.events(any())).thenReturn(listOf(event.eventType))
    whenever(authorisationConfig.consumers).thenReturn(
      mapOf(
        "mockConsumer1" to ConsumerConfig(queueName = "mockQueue1"),
        "mockConsumer2" to ConsumerConfig(queueName = "mockQueue2"),
      ),
    )
    whenever(hmppsQueueService.findByQueueId("mockQueue1")).thenReturn(mockQueue)
    whenever(mockQueue.queueArn).thenReturn("mockARN1")
    whenever(hmppsQueueService.findByQueueId("mockQueue2")).thenReturn(mockQueue)
    whenever(mockQueue.queueArn).thenReturn("mockARN2")
    whenever(mockQueue.sqsClient).thenReturn(sqsClient)

    eventNotificationService = EventNotificationService(hmppsQueueService, objectMapper, authorisationConfig, featureFlagConfig, telemetryService)
  }

  @Test
  fun `Publish Event to multiple queues is successful`() {
    whenever(sqsClient.sendMessage(any<SendMessageRequest>())).thenReturn(CompletableFuture.completedFuture(response))
    whenever(sqsClient.sendMessage(any<SendMessageRequest>())).thenReturn(CompletableFuture.completedFuture(response))
    eventNotificationService.sendEvent(event)
    argumentCaptor<SendMessageRequest>().apply {
      verify(sqsClient, times(2)).sendMessage(capture())
      val sqsMessage1 = objectMapper.readValue(firstValue.messageBody(), DirectSQSMessage::class.java)
      val message1 = objectMapper.readValue(sqsMessage1.message, EventNotification::class.java)
      val sqsMessage2 = objectMapper.readValue(firstValue.messageBody(), DirectSQSMessage::class.java)
      val message2 = objectMapper.readValue(sqsMessage2.message, EventNotification::class.java)
      assertThat(message1.eventType).isEqualTo(event.eventType)
      assertThat(message1.hmppsId).isEqualTo(event.hmppsId)
      assertThat(message1.prisonId).isEqualTo(event.prisonId)
      assertThat(message1.url).isEqualTo(event.url)
      assertThat(message2.eventType).isEqualTo(event.eventType)
      assertThat(message2.hmppsId).isEqualTo(event.hmppsId)
      assertThat(message2.prisonId).isEqualTo(event.prisonId)
      assertThat(message2.url).isEqualTo(event.url)
    }
  }

  @Test
  fun `Publish Event to first queue is unsuccessful, but second queue is successful`() {
    whenever(sqsClient.sendMessage(any<SendMessageRequest>())).thenReturn(CompletableFuture.completedFuture(response))
    whenever(hmppsQueueService.findByQueueId("mockQueue1")).thenThrow(RuntimeException("cant find queue mockQueue1"))

    eventNotificationService.sendEvent(event)
    argumentCaptor<SendMessageRequest>().apply {
      verify(sqsClient, times(1)).sendMessage(capture())
      val sqsMessage = objectMapper.readValue(firstValue.messageBody(), DirectSQSMessage::class.java)
      val message = objectMapper.readValue(sqsMessage.message, EventNotification::class.java)

      assertThat(message.eventType).isEqualTo(event.eventType)
      assertThat(message.hmppsId).isEqualTo(event.hmppsId)
      assertThat(message.prisonId).isEqualTo(event.prisonId)
      assertThat(message.url).isEqualTo(event.url)
    }

    argumentCaptor<Throwable>().apply {
      verify(telemetryService, times(1)).captureException(capture())
      firstValue.message.shouldContain("cant find queue mockQueue1")
    }
  }

  @Test
  fun `event is applicable to the consumer`() {
    val testEvent = EventNotification(url = "url", eventType = "PERSON_STATUS_CHANGED")
    val config =
      parseConfig<AuthorisationConfig>(
        """
        consumers:
          tester:
            roles:
              - full-access
        """.trimIndent(),
      )

    eventNotificationService = EventNotificationService(hmppsQueueService, objectMapper, config, featureFlagConfig, telemetryService)
    Assertions.assertTrue(eventNotificationService.isEventApplicable("tester", testEvent))
  }

  @Test
  fun `event is NOT applicable to the consumer based on event type`() {
    val testEvent = EventNotification(url = "url", eventType = "UNKNOWN_TYPE")
    val config =
      parseConfig<AuthorisationConfig>(
        """
        consumers:
          tester:
            roles:
              - full-access
        """.trimIndent(),
      )
    eventNotificationService = EventNotificationService(hmppsQueueService, objectMapper, config, featureFlagConfig, telemetryService)
    Assertions.assertFalse(eventNotificationService.isEventApplicable("tester", testEvent))
  }

  @Test
  fun `event is applicable to the consumer based on the prison id`() {
    val testEvent = EventNotification(url = "url", eventType = "PERSON_ADDRESS_CHANGED", prisonId = "MKI")
    val config =
      parseConfig<AuthorisationConfig>(
        """
        consumers:
          tester:
            roles:
              - private-prison
            filters:
              prisons:
               - MKI
        """.trimIndent(),
      )
    eventNotificationService = EventNotificationService(hmppsQueueService, objectMapper, config, featureFlagConfig, telemetryService)
    Assertions.assertTrue(eventNotificationService.isEventApplicable("tester", testEvent))
  }

  @Test
  fun `event is NOT applicable to the consumer based on the prison id`() {
    val testEvent = EventNotification(url = "url", eventType = "PERSON_ADDRESS_CHANGED", prisonId = "MDI")
    val config =
      parseConfig<AuthorisationConfig>(
        """
        consumers:
          tester:
            roles:
              - private-prison
            filters:
              prisons:
                - MKI
        """.trimIndent(),
      )
    eventNotificationService = EventNotificationService(hmppsQueueService, objectMapper, config, featureFlagConfig, telemetryService)
    Assertions.assertFalse(eventNotificationService.isEventApplicable("tester", testEvent))
  }
}
