package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.messaging

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.mock
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.messaging.sqs.AwsQueueService
import uk.gov.justice.hmpps.sqs.HmppsQueue
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import kotlin.test.Test

class AwsQueueServiceTest {
  val hmppsQueueService: HmppsQueueService = mock()
  val awsQueueService = AwsQueueService(hmppsQueueService)
  val sqsQueue: HmppsQueue = mock()
  val sqsClient: SqsAsyncClient = mock()

  @BeforeEach
  fun setup() {
    whenever(sqsQueue.sqsClient).thenReturn(sqsClient)
    whenever(sqsQueue.queueName).thenReturn("testQueue")
    whenever(sqsQueue.queueUrl).thenReturn("testQueueUrl")
    whenever(hmppsQueueService.findByQueueId("testQueue")).thenReturn(sqsQueue)
  }

  @Test
  fun `Sends to queue with event type attribute`() {
    val sendMessageRequest = argumentCaptor<SendMessageRequest>()
    awsQueueService.sendMessageToQueue("A message", "testQueue", "EVENT_TYPE")
    verify(sqsClient, times(1)).sendMessage(sendMessageRequest.capture())
    val message = sendMessageRequest.firstValue
    val attributeValue = message.messageAttributes()["eventType"]?.stringValue()
    assertThat(message.queueUrl()).isEqualTo("testQueueUrl")
    assertThat(message.messageBody()).isEqualTo("A message")
    assertThat(attributeValue).isEqualTo("EVENT_TYPE")
  }

  @Test
  fun `Sends to queue without event type attribute`() {
    val sendMessageRequest = argumentCaptor<SendMessageRequest>()
    awsQueueService.sendMessageToQueue("A message", "testQueue")
    verify(sqsClient, times(1)).sendMessage(sendMessageRequest.capture())
    val message = sendMessageRequest.firstValue
    val attributeValue = message.messageAttributes()["eventType"]?.stringValue()
    assertThat(message.queueUrl()).isEqualTo("testQueueUrl")
    assertThat(message.messageBody()).isEqualTo("A message")
    assertThat(attributeValue).isEqualTo(null)
  }
}
