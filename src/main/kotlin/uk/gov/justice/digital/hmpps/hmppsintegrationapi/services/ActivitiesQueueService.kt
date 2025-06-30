package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.MessageFailedException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ActivitiesGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.AttendanceUpdateRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsMessage
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsMessageResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonerAllocationRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonerDeallocationRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.WaitingListSearchRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.toHmppsMessage
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.toTestMessage
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.hmpps.sqs.HmppsQueue
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.eventTypeMessageAttributes
import java.time.LocalDate

@Service
class ActivitiesQueueService(
  @Autowired private val hmppsQueueService: HmppsQueueService,
  @Autowired private val objectMapper: ObjectMapper,
  @Autowired private val consumerPrisonAccessService: ConsumerPrisonAccessService,
  @Autowired private val getAttendanceByIdService: GetAttendanceByIdService,
  @Autowired private val getScheduleDetailsService: GetScheduleDetailsService,
  @Autowired private val getPrisonPayBandsService: GetPrisonPayBandsService,
  @Autowired private val getWaitingListApplicationsService: GetWaitingListApplicationsService,
  @Autowired val activitiesGateway: ActivitiesGateway,
) {
  private val activitiesQueue by lazy { hmppsQueueService.findByQueueId("activities") as HmppsQueue }
  private val activitiesQueueSqsClient by lazy { activitiesQueue.sqsClient }
  private val activitiesQueueUrl by lazy { activitiesQueue.queueUrl }

  fun sendAttendanceUpdateRequest(
    attendanceUpdateRequests: List<AttendanceUpdateRequest>,
    who: String,
    filters: ConsumerFilters?,
  ): Response<HmppsMessageResponse?> {
    for (attendanceUpdateRequest in attendanceUpdateRequests) {
      if (attendanceUpdateRequest.status == "TestEvent") {
        val testMessage = attendanceUpdateRequests.toTestMessage(actionedBy = who)
        writeMessageToQueue(testMessage, "Could not send attendance update to queue")

        return Response(HmppsMessageResponse(message = "Attendance update written to queue"))
      }

      val consumerPrisonFilterCheck = consumerPrisonAccessService.checkConsumerHasPrisonAccess<HmppsMessageResponse>(attendanceUpdateRequest.prisonId, filters)
      if (consumerPrisonFilterCheck.errors.isNotEmpty()) {
        return consumerPrisonFilterCheck
      }

      val attendanceRecordResponse = getAttendanceByIdService.execute(attendanceUpdateRequest.id, filters)
      if (attendanceRecordResponse.errors.isNotEmpty()) {
        return Response(data = null, errors = attendanceRecordResponse.errors)
      }
    }

    val hmppsMessage = attendanceUpdateRequests.toHmppsMessage(who)
    writeMessageToQueue(hmppsMessage, "Could not send attendance update to queue")

    return Response(HmppsMessageResponse(message = "Attendance update written to queue"))
  }

  fun sendPrisonerDeallocationRequest(
    scheduleId: Long,
    prisonerDeallocationRequest: PrisonerDeallocationRequest,
    who: String,
    filters: ConsumerFilters?,
  ): Response<HmppsMessageResponse?> {
    if (prisonerDeallocationRequest.reasonCode == "TestEvent") {
      val testMessage = prisonerDeallocationRequest.toTestMessage(actionedBy = who)
      writeMessageToQueue(testMessage, "Could not send prisoner deallocation to queue")

      return Response(HmppsMessageResponse(message = "Prisoner deallocation written to queue"))
    }

    val getScheduleResponse = getScheduleDetailsService.execute(scheduleId, filters)
    if (getScheduleResponse.errors.isNotEmpty()) {
      return Response(data = null, errors = getScheduleResponse.errors)
    }

    val schedule = getScheduleResponse.data ?: return Response(data = null, errors = listOf(UpstreamApiError(UpstreamApi.ACTIVITIES, UpstreamApiError.Type.ENTITY_NOT_FOUND)))

    schedule.allocations.filter { it.prisonerNumber == prisonerDeallocationRequest.prisonerNumber && it.status != "ENDED" }.ifEmpty { null }
      ?: return Response(data = null, errors = listOf(UpstreamApiError(UpstreamApi.ACTIVITIES, UpstreamApiError.Type.ENTITY_NOT_FOUND, "Allocations not found for prisoner: ${prisonerDeallocationRequest.prisonerNumber}")))

    if (prisonerDeallocationRequest.endDate.isAfter(LocalDate.parse(schedule.endDate))) {
      return Response(data = null, errors = listOf(UpstreamApiError(UpstreamApi.ACTIVITIES, UpstreamApiError.Type.BAD_REQUEST, "Passed in end date cannot be after the end date of the schedule: ${schedule.endDate}")))
    }

    val hmppsMessage = prisonerDeallocationRequest.toHmppsMessage(who, scheduleId)
    writeMessageToQueue(hmppsMessage, "Could not send prisoner deallocation to queue")

    return Response(HmppsMessageResponse(message = "Prisoner deallocation written to queue"))
  }

  fun sendPrisonerAllocationRequest(
    scheduleId: Long,
    prisonerAllocationRequest: PrisonerAllocationRequest,
    who: String,
    filters: ConsumerFilters?,
  ): Response<HmppsMessageResponse?> {
    val today = LocalDate.now()
    if (prisonerAllocationRequest.startDate!! < today) {
      return Response(data = null, errors = listOf(UpstreamApiError(UpstreamApi.ACTIVITIES, UpstreamApiError.Type.BAD_REQUEST, "Allocation start date must not be in the past")))
    }

    if (prisonerAllocationRequest.endDate != null && prisonerAllocationRequest.startDate > prisonerAllocationRequest.endDate) {
      return Response(data = null, errors = listOf(UpstreamApiError(UpstreamApi.ACTIVITIES, UpstreamApiError.Type.BAD_REQUEST, "Allocation start date must be the same as or before the end date")))
    }

    if (prisonerAllocationRequest.startDate == today && prisonerAllocationRequest.scheduleInstanceId == null) {
      return Response(data = null, errors = listOf(UpstreamApiError(UpstreamApi.ACTIVITIES, UpstreamApiError.Type.BAD_REQUEST, "scheduleInstanceId must be provided when allocation start date is today")))
    }

    if (prisonerAllocationRequest.exclusions?.any { it.startTime > it.endTime } == true) {
      return Response(data = null, errors = listOf(UpstreamApiError(UpstreamApi.ACTIVITIES, UpstreamApiError.Type.BAD_REQUEST, "Exclusion start time cannot be after custom end time")))
    }

    val getScheduleByIdResponse = activitiesGateway.getActivityScheduleById(scheduleId)

    if (getScheduleByIdResponse.errors.isNotEmpty()) {
      return Response(data = null, errors = getScheduleByIdResponse.errors)
    }

    val isPaid = getScheduleByIdResponse.data?.activity?.paid
    val payBandId = prisonerAllocationRequest.payBandId

    if (isPaid == true && payBandId == null) {
      return Response(
        data = null,
        errors =
          listOf(
            UpstreamApiError(
              UpstreamApi.ACTIVITIES,
              UpstreamApiError.Type.BAD_REQUEST,
              "Allocation must have a pay band when the activity is paid",
            ),
          ),
      )
    }

    if (isPaid == false && payBandId != null) {
      return Response(
        data = null,
        errors =
          listOf(
            UpstreamApiError(
              UpstreamApi.ACTIVITIES,
              UpstreamApiError.Type.BAD_REQUEST,
              "Allocation cannot have a pay band when the activity is unpaid",
            ),
          ),
      )
    }
    val prisonCode = getScheduleByIdResponse.data!!.activity.prisonCode
    if (isPaid == true && payBandId != null) {
      val payBandsResponse = getPrisonPayBandsService.execute(prisonCode, filters)
      if (payBandsResponse.errors.isNotEmpty()) return Response(data = null, errors = payBandsResponse.errors)

      val isValid = payBandsResponse.data!!.any { it.id == payBandId }
      if (!isValid) {
        return Response(
          data = null,
          errors =
            listOf(
              UpstreamApiError(
                UpstreamApi.ACTIVITIES,
                UpstreamApiError.Type.BAD_REQUEST,
                "Pay band '$payBandId' does not exist for prison '$prisonCode'",
              ),
            ),
        )
      }
    }

//    Allocation Start date and end date must be within the start and end date of an activity
    val scheduleStart = LocalDate.parse(getScheduleByIdResponse.data.startDate)
    val scheduleEnd = LocalDate.parse(getScheduleByIdResponse.data.endDate)

    if (prisonerAllocationRequest.startDate < scheduleStart) {
      return Response(
        data = null,
        errors =
          listOf(
            UpstreamApiError(
              UpstreamApi.ACTIVITIES,
              UpstreamApiError.Type.BAD_REQUEST,
              "Allocation start date must not be before the activity schedule start date ($scheduleStart)",
            ),
          ),
      )
    }

    if (prisonerAllocationRequest.endDate != null && prisonerAllocationRequest.endDate > scheduleEnd) {
      return Response(
        data = null,
        errors =
          listOf(
            UpstreamApiError(
              UpstreamApi.ACTIVITIES,
              UpstreamApiError.Type.BAD_REQUEST,
              "Allocation end date must not be after the activity schedule end date ($scheduleEnd)",
            ),
          ),
      )
    }

    if (prisonerAllocationRequest.endDate != null && prisonerAllocationRequest.endDate < prisonerAllocationRequest.startDate) {
      return Response(
        data = null,
        errors =
          listOf(
            UpstreamApiError(
              UpstreamApi.ACTIVITIES,
              UpstreamApiError.Type.BAD_REQUEST,
              "Allocation end date cannot be before allocation start date",
            ),
          ),
      )
    }

    if (prisonerAllocationRequest.startDate > scheduleEnd) {
      return Response(
        data = null,
        errors =
          listOf(
            UpstreamApiError(
              UpstreamApi.ACTIVITIES,
              UpstreamApiError.Type.BAD_REQUEST,
              "Allocation start date cannot be after the activity end date ($scheduleEnd)",
            ),
          ),
      )
    }

    val prisonerNumber = prisonerAllocationRequest.prisonerNumber
    val description = getScheduleByIdResponse.data.description
    val prisonerStatus =
      getScheduleByIdResponse.data.allocations
        .find { it.prisonerNumber == prisonerNumber }
        ?.status
    val isPrisonerAlreadyInSchedule = getScheduleByIdResponse.data.allocations.none { it.prisonerNumber == prisonerNumber }

    if (!isPrisonerAlreadyInSchedule && prisonerStatus != "ENDED") {
      return Response(
        data = null,
        errors =
          listOf(
            UpstreamApiError(
              UpstreamApi.ACTIVITIES,
              UpstreamApiError.Type.BAD_REQUEST,
              "Prisoner with $prisonerNumber is already allocated to schedule $description.",
            ),
          ),
      )
    }

    prisonerAllocationRequest.exclusions?.forEach { exclusion ->
      val noMatchingSlot =
        getScheduleByIdResponse.data.slots.none {
          it.weekNumber == exclusion.weekNumber &&
            it.timeSlot == exclusion.timeSlot &&
            it.daysOfWeek.containsAll(exclusion.daysOfWeek)
        }

      if (noMatchingSlot) {
        return Response(
          data = null,
          errors =
            listOf(
              UpstreamApiError(
                UpstreamApi.ACTIVITIES,
                UpstreamApiError.Type.BAD_REQUEST,
                "No ${exclusion.timeSlot} slots in week number ${exclusion.weekNumber} for schedule $scheduleId",
              ),
            ),
        )
      }
    }
    val pendingWaitingListSearchRequest = WaitingListSearchRequest(prisonerNumbers = listOf(prisonerNumber!!), status = listOf("PENDING"))
    val approvedWaitingListSearchRequest = WaitingListSearchRequest(prisonerNumbers = listOf(prisonerNumber), status = listOf("APPROVED"))
    val pendingWaitListApplicationsResponse = getWaitingListApplicationsService.execute(prisonCode, pendingWaitingListSearchRequest, filters)
    val approvedWaitListApplicationsResponse = getWaitingListApplicationsService.execute(prisonCode, approvedWaitingListSearchRequest, filters)

    if (pendingWaitListApplicationsResponse.errors.isNotEmpty()) {
      return Response(data = null, errors = pendingWaitListApplicationsResponse.errors)
    }

    if (approvedWaitListApplicationsResponse.errors.isNotEmpty()) {
      return Response(data = null, errors = approvedWaitListApplicationsResponse.errors)
    }

    val pendingWaitingListApplications = pendingWaitListApplicationsResponse.data?.content
    val approvedWaitingListApplications = approvedWaitListApplicationsResponse.data?.content

    if (!pendingWaitingListApplications.isNullOrEmpty()) {
      return Response(
        data = null,
        errors =
          listOf(
            UpstreamApiError(
              UpstreamApi.ACTIVITIES,
              UpstreamApiError.Type.CONFLICT,
              "Prisoner has a PENDING waiting list application. It must be APPROVED before they can be allocated.",
            ),
          ),
      )
    }

    if (approvedWaitingListApplications != null && approvedWaitingListApplications.size > 1) {
      return Response(
        data = null,
        errors =
          listOf(
            UpstreamApiError(
              UpstreamApi.ACTIVITIES,
              UpstreamApiError.Type.CONFLICT,
              "Prisoner has more than one APPROVED waiting list application. A prisoner can only have one approved waiting list application.",
            ),
          ),
      )
    }

    val hmppsMessage = prisonerAllocationRequest.toHmppsMessage(who, scheduleId)
    writeMessageToQueue(hmppsMessage, "Could not send prisoner allocation to queue")

    return Response(HmppsMessageResponse(message = "Prisoner allocation written to queue"))
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
          .queueUrl(activitiesQueueUrl)
          .messageBody(stringifiedMessage)
          .eventTypeMessageAttributes(hmppsMessage.eventType.toString())
          .build()

      activitiesQueueSqsClient.sendMessage(sendMessageRequest)
    } catch (e: Exception) {
      throw MessageFailedException(exceptionMessage, e)
    }
  }
}
