package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import io.kotest.matchers.shouldBe
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import software.amazon.awssdk.services.sqs.model.Message
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.messaging.QueueService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.TestQueueConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.TestQueueService
import uk.gov.justice.hmpps.sqs.HmppsQueueService

@Import(TestQueueConfig::class)
abstract class IntegrationTestInMemoryQueueBase(
  val queueName: String,
) : IntegrationTestBase() {
  @Autowired
  protected lateinit var hmppsQueueService: HmppsQueueService

  @Autowired
  protected lateinit var queueService: QueueService

  internal val testQueueService by lazy { queueService as TestQueueService }
  internal val testQueue by lazy { testQueueService.getQueue(queueName) }

  fun getNumberOfMessagesCurrentlyOnQueue(): Int = testQueue.countMessagesOnQueue()

  fun checkQueueIsEmpty() {
    await untilCallTo { getNumberOfMessagesCurrentlyOnQueue() } matches { it == 0 }
    getNumberOfMessagesCurrentlyOnQueue().shouldBe(0)
  }

  fun getQueueMessages(): List<Message> = testQueue.messages.map { Message.builder().body(it).build() }

  @BeforeEach
  fun `clear queues`() {
    testQueue.purge()
  }
}
