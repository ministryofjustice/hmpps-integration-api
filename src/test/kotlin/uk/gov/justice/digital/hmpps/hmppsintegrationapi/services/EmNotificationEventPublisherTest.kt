package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import software.amazon.awssdk.services.sns.SnsAsyncClient
import software.amazon.awssdk.services.sns.model.PublishRequest
import software.amazon.awssdk.services.sns.model.PublishResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.defaultObjectMapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.up3.CaseStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.up3.CaseStatusReason
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.up3.CaseStatusUpdate
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.HmppsTopic
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class EmNotificationEventPublisherTest {
  private val objectMapper: ObjectMapper = defaultObjectMapper
  private val snsClient = mock<SnsAsyncClient>()
  private val queueService = mock<HmppsQueueService>()
  private val topic = HmppsTopic("emnotificationevents", "arn:aws:sns:eu-west-2:000000000000:em-notification-events-topic", snsClient)
  private val clock = Clock.fixed(Instant.parse("2026-09-22T10:42:31Z"), ZoneOffset.UTC)
  private val publisher = EmNotificationEventPublisher(queueService, objectMapper, clock)
  private val request =
    CaseStatusUpdate(
      status = CaseStatus.REJECTED,
      reasons = listOf(CaseStatusReason("duplicate_submission", "Already submitted")),
      datetimeOfStatusChange = Instant.parse("2023-10-27T14:30:00Z").atOffset(ZoneOffset.UTC),
    )

  init {
    whenever(queueService.findByTopicId("emnotificationevents")).thenReturn(topic)
  }

  @Test
  fun `publishes original payload in a versioned event envelope`() {
    whenever(snsClient.publish(any<PublishRequest>())).thenReturn(
      CompletableFuture.completedFuture(PublishResponse.builder().messageId("message-1").build()),
    )

    publisher.publish("case-123", request)

    val publishRequest = argumentCaptor<PublishRequest>()
    verify(snsClient).publish(publishRequest.capture())
    val event = objectMapper.readTree(publishRequest.firstValue.message())

    assertEquals("electronic-monitoring.case-status-returned", event["eventType"].textValue())
    assertEquals("hmpps-external-api", event["source"].textValue())
    assertEquals(1, event["version"].intValue())
    assertEquals("case-123", event["data"]["caseId"].textValue())
    assertEquals("rejected", event["data"]["status"].textValue())
    assertEquals("duplicate_submission", event["data"]["reasons"][0]["section"].textValue())
    assertEquals("Already submitted", event["data"]["reasons"][0]["details"].textValue())
    assertEquals("2023-10-27T14:30Z", event["data"]["datetimeOfStatusChange"].textValue())
    assertTrue(event["eventId"].textValue().startsWith("sha256:"))
    assertEquals(
      event["eventType"].textValue(),
      publishRequest.firstValue.messageAttributes()["eventType"]?.stringValue(),
    )
  }

  @Test
  fun `uses the same event id when the same payload is retried`() {
    whenever(snsClient.publish(any<PublishRequest>())).thenReturn(
      CompletableFuture.completedFuture(PublishResponse.builder().messageId("message-1").build()),
    )

    publisher.publish("case-123", request)
    publisher.publish("case-123", request)

    val publishRequests = argumentCaptor<PublishRequest>()
    verify(snsClient, times(2)).publish(publishRequests.capture())
    val firstEventId = objectMapper.readTree(publishRequests.allValues[0].message())["eventId"].textValue()
    val secondEventId = objectMapper.readTree(publishRequests.allValues[1].message())["eventId"].textValue()

    assertEquals(firstEventId, secondEventId)
  }

  @Test
  fun `propagates SNS publication failures`() {
    val failure = RuntimeException("SNS unavailable")
    whenever(snsClient.publish(any<PublishRequest>())).thenReturn(
      CompletableFuture.failedFuture(failure),
    )

    val exception =
      assertFailsWith<ExecutionException> {
        publisher.publish("case-123", request)
      }

    assertNotNull(exception.cause)
    assertEquals("SNS unavailable", exception.cause?.message)
  }
}
