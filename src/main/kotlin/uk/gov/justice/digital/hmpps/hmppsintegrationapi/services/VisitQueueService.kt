package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.MessageFailedException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PersonalRelationshipsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CancelVisitRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CreateVisitRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsMessage
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsMessageResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpdateVisitRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UserType
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Visitor
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
  @Autowired private val personalRelationshipsGateway: PersonalRelationshipsGateway,
  @Autowired private val consumerPrisonAccessService: ConsumerPrisonAccessService,
) {
  private val visitsQueue by lazy { hmppsQueueService.findByQueueId("visits") as HmppsQueue }
  private val visitsQueueSqsClient by lazy { visitsQueue.sqsClient }
  private val visitsQueueUrl by lazy { visitsQueue.queueUrl }

  fun sendCreateVisit(
    visit: CreateVisitRequest,
    who: String,
    filters: ConsumerFilters?,
  ): Response<HmppsMessageResponse?> {
    val visitPrisonerId = visit.prisonerId
    val personResponse = getPersonService.getNomisNumberWithPrisonFilter(hmppsId = visitPrisonerId, filters = filters)
    if (personResponse.errors.isNotEmpty()) {
      return Response(data = null, errors = personResponse.errors)
    }

    if (filters?.prisons != null) {
      val consumerPrisonFilterCheck = consumerPrisonAccessService.checkConsumerHasPrisonAccess<HmppsMessageResponse>(visit.prisonId, filters)
      if (consumerPrisonFilterCheck.errors.isNotEmpty()) {
        return consumerPrisonFilterCheck
      }
    }

    val nomisNumber = personResponse.data?.nomisNumber ?: return Response(data = null, errors = listOf(UpstreamApiError(UpstreamApi.NOMIS, UpstreamApiError.Type.ENTITY_NOT_FOUND)))
    val visitors = visit.visitors.orEmpty()
    val checkVisitorsResponse = checkVisitors(nomisNumber, visitors)
    if (checkVisitorsResponse.errors.isNotEmpty()) {
      return Response(data = null, errors = checkVisitorsResponse.errors)
    }

    val hmppsMessage = visit.toHmppsMessage(who)
    writeMessageToQueue(hmppsMessage, "Could not send Visit create to queue")

    return Response(HmppsMessageResponse(message = "Visit creation written to queue"))
  }

  fun sendUpdateVisit(
    visitReference: String,
    visit: UpdateVisitRequest,
    who: String,
    filters: ConsumerFilters?,
  ): Response<HmppsMessageResponse?> {
    val visitResponse = getVisitInformationByReferenceService.execute(visitReference, filters)
    if (visitResponse.errors.isNotEmpty()) {
      return Response(data = null, errors = visitResponse.errors)
    }

    val nomisNumber = visitResponse.data?.prisonerId ?: return Response(data = null, errors = listOf(UpstreamApiError(UpstreamApi.NOMIS, UpstreamApiError.Type.ENTITY_NOT_FOUND)))
    val visitors = visit.visitors.orEmpty()
    val checkVisitorsResponse = checkVisitors(nomisNumber, visitors)
    if (checkVisitorsResponse.errors.isNotEmpty()) {
      return Response(data = null, errors = checkVisitorsResponse.errors)
    }

    val hmppsMessage = visit.toHmppsMessage(who, visitReference)
    writeMessageToQueue(hmppsMessage, "Could not send Visit update to queue")

    return Response(HmppsMessageResponse(message = "Visit update written to queue"))
  }

  fun sendCancelVisit(
    visitReference: String,
    cancelVisitRequest: CancelVisitRequest,
    who: String,
    filters: ConsumerFilters?,
  ): Response<HmppsMessageResponse?> {
    val visitResponse = getVisitInformationByReferenceService.execute(visitReference, filters)

    if (visitResponse.errors.isNotEmpty()) {
      return Response(data = null, errors = visitResponse.errors)
    }

    val prisonerId = visitResponse.data?.prisonerId ?: return Response(data = null, errors = listOf(UpstreamApiError(UpstreamApi.MANAGE_PRISON_VISITS, UpstreamApiError.Type.ENTITY_NOT_FOUND)))

    val actionedBy = if (cancelVisitRequest.userType == UserType.PRISONER) prisonerId else null
    val hmppsMessage = cancelVisitRequest.toHmppsMessage(who, visitReference, actionedBy)
    writeMessageToQueue(hmppsMessage, "Could not send Visit cancellation to queue")

    return Response(HmppsMessageResponse(message = "Visit cancellation written to queue"))
  }

  private fun writeMessageToQueue(
    hmppsMessage: HmppsMessage,
    exceptionMessage: String,
  ) {
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
    } catch (e: Exception) {
      throw MessageFailedException(exceptionMessage, e)
    }
  }

  private fun checkVisitors(
    nomisNumber: String,
    visitors: Set<Visitor>,
  ): Response<Nothing?> {
    if (visitors.isEmpty()) {
      return Response(data = null)
    }

    var page = 1
    var isLastPage = false
    val contacts =
      buildList {
        while (!isLastPage) {
          val response = personalRelationshipsGateway.getContacts(nomisNumber, page, size = 10)
          if (response.errors.isNotEmpty()) {
            return Response(data = null, errors = response.errors)
          }
          addAll(response.data?.contacts.orEmpty())
          isLastPage = response.data?.last ?: true
          page++
        }
      }

    visitors.forEach {
      if (it.nomisPersonId !in contacts.map { contact -> contact.contactId }) {
        return Response(data = null, errors = listOf(UpstreamApiError(UpstreamApi.PERSONAL_RELATIONSHIPS, UpstreamApiError.Type.ENTITY_NOT_FOUND, description = "No contact found with an ID of ${it.nomisPersonId}")))
      }
    }

    return Response(data = null)
  }
}
