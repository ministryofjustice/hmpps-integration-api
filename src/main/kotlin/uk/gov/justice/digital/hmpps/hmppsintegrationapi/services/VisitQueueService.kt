package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.MessageFailedException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CreateVisitRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsMessageResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.hmpps.sqs.HmppsQueue
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.eventTypeMessageAttributes

@Service
class VisitQueueService(
  private val getPersonService: GetPersonService,
  private val hmppsQueueService: HmppsQueueService,
) {
  private val visitsQueue by lazy { hmppsQueueService.findByQueueId("") as HmppsQueue }
  private val visitsQueueSqsClient by lazy { visitsQueue.sqsClient }
  private val visitsQueueUrl by lazy { visitsQueue.queueUrl }

  companion object {
    private val objectMapper: ObjectMapper = jacksonObjectMapper()
  }

  fun sendCreateVisit(
    visit: CreateVisitRequest,
    who: String,
    consumerFilters: ConsumerFilters?,
  ): Response<HmppsMessageResponse?> {
    val visitPrisonerId = visit.prisonerId

    val personResponse = getPersonService.getNomisNumberWithPrisonFilter(hmppsId = visitPrisonerId, filters = consumerFilters)

    if (personResponse.errors.isNotEmpty()) {
      return Response(data = null, errors = personResponse.errors)
    }

    val message = visit.toHmppsMessage(who)

    try {
      val stringifiedMessage = objectMapper.writeValueAsString(message)

      visitsQueueSqsClient.sendMessage(
        SendMessageRequest
          .builder()
          .queueUrl(visitsQueueUrl)
          .messageBody(stringifiedMessage)
          .eventTypeMessageAttributes(message.eventType.toString())
          .build(),
      )

      return Response(HmppsMessageResponse(message = "Visit creation written to queue"))
    } catch (e: Exception) {
      throw MessageFailedException("Could not send Visit message to queue", e)
    }
  }
}
