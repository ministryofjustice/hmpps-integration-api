package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.messaging.sqs

import org.springframework.stereotype.Component
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.messaging.QueueService
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.eventTypeMessageAttributes

@Component
class AwsQueueService(
  private val hmppsQueueService: HmppsQueueService,
) : QueueService {
  override fun sendMessageToQueue(
    rawMessage: String,
    queueName: String,
    eventType: String?,
  ) {
    val queue = hmppsQueueService.findByQueueId(queueName)
    val sqsClient = queue?.sqsClient!!
    val messageRequestBuilder =
      SendMessageRequest
        .builder()
        .queueUrl(queue.queueUrl)
        .messageBody(rawMessage)
    if (eventType != null) {
      messageRequestBuilder.eventTypeMessageAttributes(eventType)
    }
    sqsClient.sendMessage(messageRequestBuilder.build())
  }
}
