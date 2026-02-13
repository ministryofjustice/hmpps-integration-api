package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.queues

import org.springframework.stereotype.Component
import uk.gov.justice.hmpps.sqs.HmppsQueueService

interface Queue {
  fun queueId(): String

  fun queueName(): String

  fun sendMessage(
    eventType: String,
    event: String,
  )

  fun messageCount(): Int

  fun lastMessage(): String?
}

@Component
class QueueProvider(
  val hmppsQueueService: HmppsQueueService? = null,
) {
  companion object {
    val registeredQueues = mutableMapOf<String, Queue>()
  }

  open fun registerQueue(queue: Queue) {
    registeredQueues[queue.queueId()] = queue
  }

  fun findByQueueId(queueId: String): Queue? {
    if (registeredQueues.containsKey(queueId)) return registeredQueues[queueId]

    if (hmppsQueueService == null) {
      throw IllegalStateException("No registered queue found for queueId: $queueId")
    }

    val awsQueue = hmppsQueueService?.findByQueueId(queueId)
    if (awsQueue == null) {
      throw IllegalStateException("No SQS queue found for queueId: $queueId")
    }

    return AwsQueue(awsQueue)
  }
}
