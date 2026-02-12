package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.queues

import uk.gov.justice.hmpps.sqs.HmppsQueue
import uk.gov.justice.hmpps.sqs.sendMessage

class AwsQueue(
  val hmppsQueue: HmppsQueue,
) : Queue {
  override fun queueName() = hmppsQueue.queueName

  override fun queueId() = hmppsQueue.queueArn!!

  override fun sendMessage(
    eventType: String,
    event: String,
  ) {
    hmppsQueue.sendMessage(eventType, event)
  }
}
