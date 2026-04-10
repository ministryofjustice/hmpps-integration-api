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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.messaging.QueueService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.TestQueueService
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.countAllMessagesOnQueue

abstract class IntegrationTestWithQueueBase(
  val queueName: String,
) : IntegrationTestBase() {
  @Autowired
  protected lateinit var hmppsQueueService: HmppsQueueService

  @Autowired
  protected lateinit var queueService: QueueService

  internal val testQueue by lazy { hmppsQueueService.findByQueueId(queueName) ?: throw RuntimeException("Queue with name $queueName doesn't exist") }
  internal val testSqsClient by lazy { testQueue.sqsClient }
  internal val testQueueUrl by lazy { testQueue.queueUrl }

  // New approach using the TestQueueService
  internal val testQueueService by lazy { queueService as TestQueueService }
  internal val newTestQueue by lazy { testQueueService.getQueue(queueName) }

  fun getNumberOfMessagesOnQueue(): Int = newTestQueue.countMessagesOnQueue()

  fun getQueueMessagesNew(): List<Message> = newTestQueue.messages.map { Message.builder().body(it).build() }

  fun getNumberOfMessagesCurrentlyOnQueue(): Int = testSqsClient.countAllMessagesOnQueue(testQueueUrl).get()

  fun checkQueueIsEmpty() {
    await untilCallTo { getNumberOfMessagesCurrentlyOnQueue() } matches { it == 0 }
    getNumberOfMessagesCurrentlyOnQueue().shouldBe(0)
  }

  fun getQueueMessages(): List<Message> = testSqsClient.receiveMessage(ReceiveMessageRequest.builder().queueUrl(testQueueUrl).build()).join().messages()

  @BeforeEach
  fun `clear queues`() {
    newTestQueue.purge()

    testSqsClient.purgeQueue(PurgeQueueRequest.builder().queueUrl(testQueueUrl).build())
  }
}
