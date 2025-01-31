package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.MessageFailedException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ExpressionOfInterest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ExpressionOfInterestMessage
import uk.gov.justice.hmpps.sqs.HmppsQueue
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.eventTypeMessageAttributes
import java.util.UUID

@Component
class PutExpressionInterestService(
  private val hmppsQueueService: HmppsQueueService,
  private val objectMapper: ObjectMapper,
) {
  private val eoiQueue by lazy { hmppsQueueService.findByQueueId("jobsboardintegration") as HmppsQueue }
  private val eoiQueueSqsClient by lazy { eoiQueue.sqsClient }
  private val eoiQueueUrl by lazy { eoiQueue.queueUrl }

  fun sendExpressionOfInterest(expressionOfInterest: ExpressionOfInterest) {
    try {
      val messageBody =
        objectMapper.writeValueAsString(
          ExpressionOfInterestMessage(
            messageId = UUID.randomUUID().toString(),
            jobId = expressionOfInterest.jobId,
            prisonNumber = expressionOfInterest.prisonNumber,
          ),
        )

      eoiQueueSqsClient.sendMessage(
        SendMessageRequest
          .builder()
          .queueUrl(eoiQueueUrl)
          .messageBody(messageBody)
          .eventTypeMessageAttributes("mjma-jobs-board.job.expression-of-interest.created")
          .build(),
      )
    } catch (e: Exception) {
      throw MessageFailedException("Failed to send message to SQS", e)
    }
  }
}
