package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.queues

import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest
import uk.gov.justice.hmpps.sqs.HmppsQueue
import uk.gov.justice.hmpps.sqs.countMessagesOnQueue
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

  override fun messageCount() = hmppsQueue.sqsClient.countMessagesOnQueue(hmppsQueue.queueUrl).get()

  override fun lastMessage(): String? {
    return hmppsQueue
      .sqsClient
      .receiveMessage(ReceiveMessageRequest.builder().queueUrl(hmppsQueue.queueUrl).build())
      .join()
      .messages()
      .last()
      .body()
  }

}
