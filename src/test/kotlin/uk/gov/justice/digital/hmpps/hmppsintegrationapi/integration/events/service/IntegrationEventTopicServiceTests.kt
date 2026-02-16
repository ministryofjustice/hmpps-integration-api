package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.events.service

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.matchers.maps.shouldNotHaveKey
import net.javacrumbs.jsonunit.assertj.JsonAssertions
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.test.context.ActiveProfiles
import software.amazon.awssdk.services.sns.SnsAsyncClient
import software.amazon.awssdk.services.sns.model.MessageAttributeValue
import software.amazon.awssdk.services.sns.model.PublishRequest
import software.amazon.awssdk.services.sns.model.PublishResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.EventNotification
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.IntegrationEventStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.services.IntegrationEventTopicService
import uk.gov.justice.hmpps.sqs.HmppsQueue
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.HmppsTopic
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture

@ActiveProfiles("test")
@JsonTest
class IntegrationEventTopicServiceTests(
  @Autowired private val objectMapper: ObjectMapper,
) {
  val hmppsQueueService: HmppsQueueService = mock()
  val hmppsEventSnsClient: SnsAsyncClient = mock()
  val mockQueue: HmppsQueue = mock()
  private lateinit var integrationEventTopicService: IntegrationEventTopicService
  val currentTime: LocalDateTime = LocalDateTime.now()

  @BeforeEach
  fun setUp() {
    whenever(hmppsQueueService.findByTopicId("integrationeventtopic"))
      .thenReturn(HmppsTopic("integrationeventtopic", "sometopicarn", hmppsEventSnsClient))
    whenever(hmppsQueueService.findByQueueId("mockQueue")).thenReturn(mockQueue)
    whenever(mockQueue.queueArn).thenReturn("mockARN")
    integrationEventTopicService = IntegrationEventTopicService(hmppsQueueService, objectMapper)
  }

  @Test
  fun `Publish Event`() {
    val event =
      EventNotification(
        eventId = 123,
        hmppsId = "hmppsId",
        eventType = "MAPPA_DETAIL_CHANGED",
        prisonId = "MKI",
        url = "mockUrl",
        lastModifiedDatetime = currentTime,
        claimId = null,
        status = IntegrationEventStatus.PROCESSING.name,
      )

    val response =
      PublishResponse
        .builder()
        .messageId("123")
        .build()

    whenever(hmppsEventSnsClient.publish(any<PublishRequest>())).thenReturn(CompletableFuture.completedFuture(response))
    integrationEventTopicService.sendEvent(event)

    argumentCaptor<PublishRequest>().apply {
      verify(hmppsEventSnsClient, times(1)).publish(capture())
      val payload = firstValue.message()
      val messageAttributes = firstValue.messageAttributes()
      JsonAssertions.assertThatJson(payload).node("eventType").isEqualTo(event.eventType)
      JsonAssertions.assertThatJson(payload).node("hmppsId").isEqualTo(event.hmppsId)
      JsonAssertions.assertThatJson(payload).node("prisonId").isEqualTo(event.prisonId)
      JsonAssertions.assertThatJson(payload).node("url").isEqualTo(event.url)
      Assertions
        .assertThat(messageAttributes["eventType"])
        .isEqualTo(
          MessageAttributeValue
            .builder()
            .stringValue(event.eventType)
            .dataType("String")
            .build(),
        )
      Assertions
        .assertThat(messageAttributes["prisonId"])
        .isEqualTo(
          MessageAttributeValue
            .builder()
            .stringValue(event.prisonId)
            .dataType("String")
            .build(),
        )
    }
  }

  @Test
  fun `Publish Event with no prison Id`() {
    val event = EventNotification(eventId = 123, hmppsId = "hmppsId", eventType = "MAPPA_DETAIL_CHANGED", prisonId = null, url = "mockUrl", lastModifiedDatetime = currentTime)

    val response =
      PublishResponse
        .builder()
        .messageId("123")
        .build()

    whenever(hmppsEventSnsClient.publish(any<PublishRequest>())).thenReturn(CompletableFuture.completedFuture(response))
    integrationEventTopicService.sendEvent(event)

    argumentCaptor<PublishRequest>().apply {
      verify(hmppsEventSnsClient, times(1)).publish(capture())
      val payload = firstValue.message()
      val messageAttributes = firstValue.messageAttributes()
      JsonAssertions.assertThatJson(payload).node("eventType").isEqualTo(event.eventType)
      JsonAssertions.assertThatJson(payload).node("hmppsId").isEqualTo(event.hmppsId)
      JsonAssertions.assertThatJson(payload).node("prisonId").isEqualTo(event.prisonId)
      JsonAssertions.assertThatJson(payload).node("url").isEqualTo(event.url)
      JsonAssertions.assertThatJson(payload).node("claimId").isNull() // Jess - this was change form isAbsent() to isNull(), I don't think its an issue but would like to check
      JsonAssertions.assertThatJson(payload).node("status").isNull()
      Assertions
        .assertThat(messageAttributes["eventType"])
        .isEqualTo(
          MessageAttributeValue
            .builder()
            .stringValue(event.eventType)
            .dataType("String")
            .build(),
        )
      messageAttributes.shouldNotHaveKey("prisonId")
    }
  }
}
