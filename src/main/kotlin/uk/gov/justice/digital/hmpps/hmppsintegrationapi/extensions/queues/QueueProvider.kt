package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.queues

import org.springframework.stereotype.Component
import uk.gov.justice.hmpps.sqs.HmppsQueueService

interface Queue {
  fun getId() : String
  fun getName() : String
  fun sendMessage(eventType: String, event: String)
}

@Component
class QueueProvider(val hmppsQueueService: HmppsQueueService? = null) {
  companion object {
    val testQueues = mutableMapOf<String, Queue>()
  }

  open fun registerQueue(queue: Queue) {
    testQueues[queue.getId()] = queue
  }

  fun findByQueueId(queueId: String): Queue? {
    if (hmppsQueueService != null) {
      return AwsQueue(hmppsQueueService?.findByQueueId(queueId)!!)
    } else {
      return testQueues[queueId]
    }
  }
}
