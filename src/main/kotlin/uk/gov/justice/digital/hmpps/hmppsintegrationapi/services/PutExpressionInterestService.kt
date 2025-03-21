package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.validation.ValidationException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.MessageFailedException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ExpressionOfInterest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsMessage
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsMessageEventType
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.hmpps.sqs.HmppsQueue
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.eventTypeMessageAttributes

@Component
class PutExpressionInterestService(
  private val getPersonService: GetPersonService,
  private val hmppsQueueService: HmppsQueueService,
  private val objectMapper: ObjectMapper,
) {
  private val eoiQueue by lazy { hmppsQueueService.findByQueueId("jobsboardintegration") as HmppsQueue }
  private val eoiQueueSqsClient by lazy { eoiQueue.sqsClient }
  private val eoiQueueUrl by lazy { eoiQueue.queueUrl }

  companion object {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun sendExpressionOfInterest(
    hmppsId: String,
    jobid: String,
  ) {
    val personResponse = getPersonService.getNomisNumber(hmppsId = hmppsId)

    if (personResponse.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      logger.debug("ExpressionOfInterest: Could not find nomis number for hmppsId: $hmppsId")
      throw EntityNotFoundException("Could not find person with id: $hmppsId")
    }

    if (personResponse.hasError(UpstreamApiError.Type.BAD_REQUEST)) {
      logger.debug("ExpressionOfInterest: Invalid hmppsId: $hmppsId")
      throw ValidationException("Invalid HMPPS ID: $hmppsId")
    }

    val nomisNumber = personResponse.data?.nomisNumber ?: run { throw ValidationException("Invalid HMPPS ID: $hmppsId") }
    val expressionOfInterest = ExpressionOfInterest(jobid, nomisNumber)

    val eventType = HmppsMessageEventType.EXPRESSION_OF_INTEREST_CREATED
    try {
      val hmppsMessage =
        objectMapper.writeValueAsString(
          HmppsMessage(
            eventType = eventType,
            messageAttributes = with(expressionOfInterest) { mapOf("jobId" to jobId, "prisonNumber" to prisonNumber) },
          ),
        )

      eoiQueueSqsClient.sendMessage(
        SendMessageRequest
          .builder()
          .queueUrl(eoiQueueUrl)
          .messageBody(hmppsMessage)
          .eventTypeMessageAttributes(eventType.type)
          .build(),
      )
    } catch (e: Exception) {
      throw MessageFailedException("Failed to send message to SQS", e)
    }
  }
}
