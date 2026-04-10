package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.sqs

import org.springframework.stereotype.Component
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import uk.gov.justice.hmpps.sqs.HmppsQueueService

interface QueueService {
  fun sendMessageToQueue(
    rawMessage: String,
    queueName: String,
  )
}

@Component
class AwsQueueService(
  private val hmppsQueueService: HmppsQueueService,
) : QueueService {
  override fun sendMessageToQueue(
    rawMessage: String,
    queueName: String,
  ) {
    val queue = hmppsQueueService.findByQueueId(queueName)
    val sqsClient = queue?.sqsClient!!
    val sendMessageRequest =
      SendMessageRequest
        .builder()
        .queueUrl(queue.queueUrl)
        .messageBody(rawMessage)
        .build()
    sqsClient.sendMessage(sendMessageRequest)
  }
}
