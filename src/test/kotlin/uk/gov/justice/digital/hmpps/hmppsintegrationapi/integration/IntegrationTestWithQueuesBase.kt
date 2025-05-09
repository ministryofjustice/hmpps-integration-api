package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import io.kotest.matchers.shouldBe
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import software.amazon.awssdk.services.sqs.model.Message
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.countAllMessagesOnQueue

abstract class IntegrationTestWithQueuesBase(
  val queueName: String,
) : IntegrationTestBase() {
  @Autowired
  protected lateinit var hmppsQueueService: HmppsQueueService

  internal val testQueue by lazy { hmppsQueueService.findByQueueId(queueName) ?: throw RuntimeException("Queue with name $queueName doesn't exist") }
  internal val testSqsClient by lazy { testQueue.sqsClient }
  internal val testQueueUrl by lazy { testQueue.queueUrl }

  fun getNumberOfMessagesCurrentlyOnQueue(): Int = testSqsClient.countAllMessagesOnQueue(testQueueUrl).get()

  fun checkQueueIsEmpty() {
    await untilCallTo { getNumberOfMessagesCurrentlyOnQueue() } matches { it == 0 }
    getNumberOfMessagesCurrentlyOnQueue().shouldBe(0)
  }

  fun getQueueMessages(): List<Message> = testSqsClient.receiveMessage(ReceiveMessageRequest.builder().queueUrl(testQueueUrl).build()).join().messages()

  @BeforeEach
  fun `clear queues`() {
    testSqsClient.purgeQueue(PurgeQueueRequest.builder().queueUrl(testQueueUrl).build())
  }
}
