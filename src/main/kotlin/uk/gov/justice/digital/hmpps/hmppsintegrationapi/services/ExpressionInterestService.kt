package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.MessageFailedException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.expressionOfInterest.ExpressionInterest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ExpressionOfInterestMessage
import uk.gov.justice.hmpps.sqs.HmppsQueue
import uk.gov.justice.hmpps.sqs.HmppsQueueService

@Service
@Component
class ExpressionInterestService(
  private val hmppsQueueService: HmppsQueueService,
  private val objectMapper: ObjectMapper,
) {
  private val eoiQueue by lazy { hmppsQueueService.findByQueueId("eoi-queue") as HmppsQueue }
  private val eoiQueueSqsClient by lazy { eoiQueue.sqsClient }
  private val eoiQueueUrl by lazy { eoiQueue.queueUrl }

  fun sendExpressionOfInterest(expressionInterest: ExpressionInterest) {
    try {
      val messageBody =
        objectMapper.writeValueAsString(
          ExpressionOfInterestMessage(
            jobId = expressionInterest.jobId,
            verifiedHmppsId = expressionInterest.hmppsId,
          ),
        )

      eoiQueueSqsClient.sendMessage(
        SendMessageRequest.builder()
          .queueUrl(eoiQueueUrl)
          .messageBody(messageBody)
          .build(),
      )
    } catch (e: Exception) {
      throw MessageFailedException("Failed to send message to SQS")
    }
  }
}
