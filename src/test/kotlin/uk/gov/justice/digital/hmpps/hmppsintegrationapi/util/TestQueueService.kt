package uk.gov.justice.digital.hmpps.hmppsintegrationapi.util

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.sqs.QueueService
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
  ) {
    val queue = hmppsQueues.firstOrNull { it.queueName == queueName } ?: throw RuntimeException("Queue $queueName not found")
    queue.publish(rawMessage)
  }

  fun getQueue(queueName: String) = hmppsQueues.firstOrNull { it.queueName == queueName } ?: throw RuntimeException("Queue $queueName not found")

  inline fun <reified T : Any> getMessagesFromQueue(queueName: String): List<T> = getQueue(queueName).messagesOnQueue<T>()
}

class TestQueue(
  val queueName: String,
) {
  var messages = mutableListOf<String>()

  fun publish(message: String) {
    messages.add(message)
  }

  inline fun <reified T : Any> messagesOnQueue(): List<T> =
    messages.map {
      val messageContent = objectMapper.readTree(it)
      objectMapper.readValue(messageContent.get("Message").textValue(), T::class.java)
    }
}
