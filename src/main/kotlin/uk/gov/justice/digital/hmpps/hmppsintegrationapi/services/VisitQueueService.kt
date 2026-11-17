package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.ForbiddenByUpstreamServiceException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.MessageFailedException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ManageUsersGateway
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.VisitNotes
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Visitor
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.hmpps.sqs.HmppsQueue
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.eventTypeMessageAttributes

@Service
class VisitQueueService(
  private val getPersonService: GetPersonService,
  private val hmppsQueueService: HmppsQueueService,
  private val objectMapper: ObjectMapper,
  private val getVisitInformationByReferenceService: GetVisitInformationByReferenceService,
  private val personalRelationshipsGateway: PersonalRelationshipsGateway,
  private val consumerPrisonAccessService: ConsumerPrisonAccessService,
  private val manageUsersGateway: ManageUsersGateway,
) {
  private val visitsQueue by lazy { hmppsQueueService.findByQueueId("visits") as HmppsQueue }
  private val visitsQueueSqsClient by lazy { visitsQueue.sqsClient }
  private val visitsQueueUrl by lazy { visitsQueue.queueUrl }

  fun sendCreateVisit(
    createVisitRequest: CreateVisitRequest,
    who: String,
    filters: ConsumerFilters?,
  ): Response<HmppsMessageResponse?> {
    val validateStaffEmail = validateUserEmail(createVisitRequest.staffEmailAddress, createVisitRequest.userType)
    if (validateStaffEmail.errors.isNotEmpty()) {
      return Response(data = null, errors = validateStaffEmail.errors)
    }

    val staffUsername =
      createVisitRequest.staffEmailAddress?.let {
        val resolveUsernameFromStaffEmail = resolveUsernameFromStaffEmail(it)
        if (resolveUsernameFromStaffEmail.errors.isNotEmpty()) {
          return Response(data = null, errors = resolveUsernameFromStaffEmail.errors)
        }
        resolveUsernameFromStaffEmail.data
      }

    val checkVisitNoteTypesUniqueResponse = checkVisitNoteTypesUnique(createVisitRequest.visitNotes)
    if (checkVisitNoteTypesUniqueResponse.errors.isNotEmpty()) {
      return Response(data = null, errors = checkVisitNoteTypesUniqueResponse.errors)
    }

    val visitPrisonerId = createVisitRequest.prisonerId
    val personResponse = getPersonService.getNomisNumber(hmppsId = visitPrisonerId, filters = filters)
    if (personResponse.errors.isNotEmpty()) {
      return Response(data = null, errors = personResponse.errors)
    }

    if (filters?.prisons != null) {
      val consumerPrisonFilterCheck = consumerPrisonAccessService.checkConsumerHasPrisonAccess<HmppsMessageResponse>(createVisitRequest.prisonId, filters)
      if (consumerPrisonFilterCheck.errors.isNotEmpty()) {
        return consumerPrisonFilterCheck
      }
    }

    val nomisNumber = personResponse.data?.nomisNumber ?: return Response(data = null, errors = listOf(UpstreamApiError(UpstreamApi.PRISON_API, UpstreamApiError.Type.ENTITY_NOT_FOUND)))
    val visitors = createVisitRequest.visitors.orEmpty()
    val checkVisitorsResponse = checkVisitors(nomisNumber, visitors)
    if (checkVisitorsResponse.errors.isNotEmpty()) {
      return Response(data = null, errors = checkVisitorsResponse.errors)
    }

    val hmppsMessage = createVisitRequest.toHmppsMessage(who, staffUsername)
    writeMessageToQueue(hmppsMessage, "Could not send Visit create to queue")

    return Response(HmppsMessageResponse(message = "Visit creation written to queue"))
  }

  fun sendUpdateVisit(
    visitReference: String,
    updateVisitRequest: UpdateVisitRequest,
    who: String,
    filters: ConsumerFilters?,
  ): Response<HmppsMessageResponse?> {
    val validateUserEmail = validateUserEmail(updateVisitRequest.staffEmailAddress, updateVisitRequest.userType)
    if (validateUserEmail.errors.isNotEmpty()) {
      return Response(data = null, errors = validateUserEmail.errors)
    }

    val staffUsername =
      updateVisitRequest.staffEmailAddress?.let {
        val resolveUsernameFromStaffEmail = resolveUsernameFromStaffEmail(it)
        if (resolveUsernameFromStaffEmail.errors.isNotEmpty()) {
          return Response(data = null, errors = resolveUsernameFromStaffEmail.errors)
        }
        resolveUsernameFromStaffEmail.data
      }

    val checkVisitNoteTypesUniqueResponse = checkVisitNoteTypesUnique(updateVisitRequest.visitNotes)
    if (checkVisitNoteTypesUniqueResponse.errors.isNotEmpty()) {
      return Response(data = null, errors = checkVisitNoteTypesUniqueResponse.errors)
    }

    val visitResponse = getVisitInformationByReferenceService.execute(visitReference, filters)
    if (visitResponse.errors.isNotEmpty()) {
      return Response(data = null, errors = visitResponse.errors)
    }

    val nomisNumber = visitResponse.data?.prisonerId ?: return Response(data = null, errors = listOf(UpstreamApiError(UpstreamApi.PRISON_API, UpstreamApiError.Type.ENTITY_NOT_FOUND)))

    if (visitResponse.data.visitExternalSystemDetails?.clientName != who) {
      throw ForbiddenByUpstreamServiceException("Visit with reference: $visitReference forbidden for $who")
    }

    val visitors = updateVisitRequest.visitors.orEmpty()
    val checkVisitorsResponse = checkVisitors(nomisNumber, visitors)
    if (checkVisitorsResponse.errors.isNotEmpty()) {
      return Response(data = null, errors = checkVisitorsResponse.errors)
    }

    val actionedBy =
      when (updateVisitRequest.userType) {
        UserType.PRISONER -> nomisNumber
        UserType.STAFF -> staffUsername
        else -> null
      }

    val hmppsMessage = updateVisitRequest.toHmppsMessage(who, visitReference, actionedBy)
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

    val validateUserEmail = validateUserEmail(cancelVisitRequest.staffEmailAddress, cancelVisitRequest.userType)
    if (validateUserEmail.errors.isNotEmpty()) {
      return Response(data = null, errors = validateUserEmail.errors)
    }

    val staffUsername =
      cancelVisitRequest.staffEmailAddress?.let {
        val resolveUsernameFromStaffEmail = resolveUsernameFromStaffEmail(it)
        if (resolveUsernameFromStaffEmail.errors.isNotEmpty()) {
          return Response(data = null, errors = resolveUsernameFromStaffEmail.errors)
        }
        resolveUsernameFromStaffEmail.data
      }

    if (visitResponse.data.visitExternalSystemDetails?.clientName != who) {
      throw ForbiddenByUpstreamServiceException("Visit with reference: $visitReference forbidden for $who")
    }

    val actionedBy =
      when (cancelVisitRequest.userType) {
        UserType.PRISONER -> prisonerId
        UserType.STAFF -> staffUsername
        else -> null
      }

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
          val response = personalRelationshipsGateway.getContacts(nomisNumber, page, size = 10, requestContext = null)
          if (response.errors.isNotEmpty()) {
            return Response(data = null, errors = response.errors)
          }
          addAll(response.data?.contacts.orEmpty())
          isLastPage = response.data?.toPaginatedPrisonerContacts()?.isLastPage ?: true
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

  fun checkVisitNoteTypesUnique(visitNotes: List<VisitNotes>): Response<Nothing?> {
    if (visitNotes.isEmpty()) {
      return Response(data = null)
    }

    val uniqueVisitNotes = visitNotes.map { it.type }.toSet()
    if (uniqueVisitNotes.size != visitNotes.size) {
      return Response(data = null, errors = listOf(UpstreamApiError(UpstreamApi.MANAGE_PRISON_VISITS, UpstreamApiError.Type.BAD_REQUEST, description = "You cannot have multiple visit notes of the same type")))
    }

    return Response(data = null)
  }

  fun validateUserEmail(
    staffEmailAddress: String? = null,
    userType: UserType? = null,
  ): Response<Nothing?> {
    if (staffEmailAddress.isNullOrBlank() && userType == UserType.STAFF) {
      return Response(data = null, errors = listOf(UpstreamApiError(UpstreamApi.MANAGE_PRISON_VISITS, UpstreamApiError.Type.BAD_REQUEST, description = "You must provide a staffEmailAddress when userType is STAFF")))
    }
    return Response(data = null)
  }

  fun resolveUsernameFromStaffEmail(staffEmailAddress: String): Response<String?> {
    val dpsUser = manageUsersGateway.getPrisonUsersByEmail(staffEmailAddress).data?.firstOrNull() ?: return Response(data = null, errors = listOf(UpstreamApiError(UpstreamApi.MANAGE_PRISON_VISITS, UpstreamApiError.Type.BAD_REQUEST, description = "No DPS username found for staff email address: $staffEmailAddress")))
    return Response(dpsUser.username)
  }
}
