package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.MessageFailedException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CancelVisitRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CreateVisitRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsMessageResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.hmpps.sqs.HmppsQueue
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.eventTypeMessageAttributes

@Service
class VisitQueueService(
  @Autowired private val getPersonService: GetPersonService,
  @Autowired private val hmppsQueueService: HmppsQueueService,
  @Autowired private val objectMapper: ObjectMapper,
  @Autowired private val getVisitInformationByReferenceService: GetVisitInformationByReferenceService,
) {
  private val visitsQueue by lazy { hmppsQueueService.findByQueueId("visits") as HmppsQueue }
  private val visitsQueueSqsClient by lazy { visitsQueue.sqsClient }
  private val visitsQueueUrl by lazy { visitsQueue.queueUrl }

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

    val hmppsMessage = visit.toHmppsMessage(who)

    try {
      val stringifiedMessage = objectMapper.writeValueAsString(hmppsMessage)
      val sendMessageRequest =
        SendMessageRequest
          .builder()
          .queueUrl(visitsQueueUrl)
          .messageBody(stringifiedMessage)
          .eventTypeMessageAttributes(hmppsMessage.eventType.toString())
          .build()

      visitsQueueSqsClient.sendMessage(sendMessageRequest)

      return Response(HmppsMessageResponse(message = "Visit creation written to queue"))
    } catch (e: Exception) {
      throw MessageFailedException("Could not send Visit message to queue", e)
    }
  }

  fun sendCancelVisit(
    visitReference: String,
    visit: CancelVisitRequest,
    who: String,
    consumerFilters: ConsumerFilters?,
  ): Response<HmppsMessageResponse?> {
    val visitResponse = getVisitInformationByReferenceService.execute(visitReference, consumerFilters)

    if (visitResponse.errors.isNotEmpty()) {
      return Response(data = null, errors = visitResponse.errors)
    }

    val hmppsMessage = visit.toHmppsMessage(who)

    try {
      val stringifiedMessage = objectMapper.writeValueAsString(hmppsMessage)
      val sendMessageRequest =
        SendMessageRequest
          .builder()
          .queueUrl(visitsQueueUrl)
          .messageBody(stringifiedMessage)
          .eventTypeMessageAttributes(hmppsMessage.eventType.toString())
          .build()

      visitsQueueSqsClient.sendMessage(sendMessageRequest)

      return Response(HmppsMessageResponse(message = "Visit cancellation written to queue"))
    } catch (e: Exception) {
      throw MessageFailedException("Could not send Visit cancellation to queue", e)
    }
  }
}
