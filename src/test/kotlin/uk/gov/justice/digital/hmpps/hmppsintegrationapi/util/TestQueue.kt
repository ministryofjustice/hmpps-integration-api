package uk.gov.justice.digital.hmpps.hmppsintegrationapi.util

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.messaging.QueueService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.MockMvcExtensions.objectMapper

/**
 * Test implementation of the QueueService
 */
class TestQueueService(
  queues: List<String> = emptyList(),
) : QueueService {
  var hmppsQueues = queues.map { TestQueue(it) }

  override fun sendMessageToQueue(
    rawMessage: String,
    queueName: String,
    eventType: String?,
  ) {
    val queue = hmppsQueues.firstOrNull { it.queueName == queueName } ?: throw RuntimeException("Queue $queueName not found")
    queue.publish(rawMessage)
  }

  fun getQueue(queueName: String) = hmppsQueues.firstOrNull { it.queueName == queueName } ?: throw RuntimeException("Queue $queueName not found")
}

class TestQueue(
  val queueName: String,
) {
  var messages = mutableListOf<String>()

  fun publish(message: String) {
    messages.add(message)
  }

  fun countMessagesOnQueue(): Int = messages.size

  fun purge() {
    messages.clear()
  }

  inline fun <reified T : Any> messagesAsObjects(): List<T> =
    messages.map {
      val messageContent = objectMapper.readTree(it)
      objectMapper.readValue(messageContent.get("Message").textValue(), T::class.java)
    }
}

@TestConfiguration
@Import(TestQueues::class)
class TestQueueConfig {
  /**
   Creating a QueueService of TestQueueService initialising with the list of queues that are currently in the sqs config
   */
  @Bean
  @Primary
  fun testQueueService(testQueueConfig: TestQueues): QueueService = TestQueueService(testQueueConfig.queues.keys.map { it })
}

@TestConfiguration
@ConfigurationProperties(prefix = "hmpps.sqs")
class TestQueues {
  var queues: Map<String, TestQueue> = emptyMap()
}
