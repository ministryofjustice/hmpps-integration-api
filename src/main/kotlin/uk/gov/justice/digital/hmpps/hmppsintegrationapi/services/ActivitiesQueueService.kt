package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.MessageFailedException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ActivitiesGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesActivityScheduleDetailed
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
  @Autowired private val getPersonService: GetPersonService,
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
    if (prisonerAllocationRequest.testEvent == "TestEvent") {
      val testMessage = prisonerAllocationRequest.toTestMessage(actionedBy = who)
      writeMessageToQueue(testMessage, "Could not send prisoner allocation to queue")

      return Response(HmppsMessageResponse(message = "Prisoner allocation written to queue"))
    }

    val today = LocalDate.now()

    validateAllocationDates(prisonerAllocationRequest, today)?.let { return it }
    validateScheduleInstanceIfToday(prisonerAllocationRequest, today)?.let { return it }
    validateExclusionTimes(prisonerAllocationRequest)?.let { return it }

    val personResponse = getPersonService.getPrisoner(prisonerAllocationRequest.prisonerNumber, filters)
    if (personResponse.errors.isNotEmpty()) {
      return Response(data = null, errors = personResponse.errors)
    }

    val scheduleResponse = activitiesGateway.getActivityScheduleById(scheduleId)
    if (scheduleResponse.errors.isNotEmpty()) return Response(data = null, errors = scheduleResponse.errors)
    val schedule = scheduleResponse.data!!

    val prisonCode = schedule.activity.prisonCode
    if (prisonCode != personResponse.data?.prisonId) {
      return Response(
        data = null,
        errors = listOf(UpstreamApiError(UpstreamApi.ACTIVITIES, UpstreamApiError.Type.BAD_REQUEST, "Unable to allocate prisoner with prisoner number ${prisonerAllocationRequest.prisonerNumber}, prisoner is not active at prison $prisonCode.")),
      )
    }

    val consumerPrisonFilterCheck = consumerPrisonAccessService.checkConsumerHasPrisonAccess<HmppsMessageResponse>(prisonCode, filters, upstreamServiceType = UpstreamApi.ACTIVITIES)
    if (consumerPrisonFilterCheck.errors.isNotEmpty()) {
      return consumerPrisonFilterCheck
    }

    validatePayBand(schedule, prisonerAllocationRequest, filters)?.let { return it }
    validateAllocationWithinScheduleDates(schedule, prisonerAllocationRequest)?.let { return it }
    validatePrisonerNotAlreadyAllocated(schedule, prisonerAllocationRequest)?.let { return it }
    validateExclusionSlots(schedule, prisonerAllocationRequest, scheduleId)?.let { return it }
    validateWaitingListApplications(schedule.activity.prisonCode, prisonerAllocationRequest.prisonerNumber, filters)?.let { return it }

    val message = prisonerAllocationRequest.toHmppsMessage(who, scheduleId)
    writeMessageToQueue(message, "Could not send prisoner allocation to queue")
    return Response(HmppsMessageResponse(message = "Prisoner allocation written to queue"))
  }

  private fun validateAllocationDates(
    request: PrisonerAllocationRequest,
    today: LocalDate,
  ): Response<HmppsMessageResponse?>? {
    if (request.startDate < today) {
      return badRequest("Allocation start date must not be in the past")
    }

    if (request.endDate != null && request.startDate > request.endDate) {
      return badRequest("Allocation start date must be the same as or before the end date")
    }

    return null
  }

  private fun validateScheduleInstanceIfToday(
    request: PrisonerAllocationRequest,
    today: LocalDate,
  ): Response<HmppsMessageResponse?>? =
    if (request.startDate == today && request.scheduleInstanceId == null) {
      badRequest("scheduleInstanceId must be provided when allocation start date is today")
    } else {
      null
    }

  private fun validateExclusionTimes(request: PrisonerAllocationRequest): Response<HmppsMessageResponse?>? {
    if (request.exclusions?.any { it.customStartTime != null && it.customEndTime != null && it.customStartTime > it.customEndTime } == true) {
      return badRequest("Exclusion start time cannot be after custom end time")
    }
    return null
  }

  private fun validatePayBand(
    schedule: ActivitiesActivityScheduleDetailed,
    request: PrisonerAllocationRequest,
    filters: ConsumerFilters?,
  ): Response<HmppsMessageResponse?>? {
    val isPaid = schedule.activity.paid
    val payBandId = request.payBandId

    if (isPaid && payBandId == null) {
      return badRequest("Allocation must have a pay band when the activity is paid")
    }

    if (!isPaid && payBandId != null) {
      return badRequest("Allocation cannot have a pay band when the activity is unpaid")
    }

    if (isPaid && payBandId != null) {
      val payBandsResponse = getPrisonPayBandsService.execute(schedule.activity.prisonCode, filters)
      if (payBandsResponse.errors.isNotEmpty()) return Response(data = null, errors = payBandsResponse.errors)
      if (payBandsResponse.data!!.none { it.id == payBandId }) {
        return badRequest("Pay band '$payBandId' does not exist for prison '${schedule.activity.prisonCode}'")
      }
    }
    return null
  }

  private fun validateAllocationWithinScheduleDates(
    schedule: ActivitiesActivityScheduleDetailed,
    request: PrisonerAllocationRequest,
  ): Response<HmppsMessageResponse?>? {
    val scheduleStart = LocalDate.parse(schedule.startDate)
    val scheduleEnd = schedule.endDate?.let { LocalDate.parse(it) }

    if (request.startDate < scheduleStart) {
      return badRequest("Allocation start date must not be before the activity schedule start date ($scheduleStart)")
    }

    if (scheduleEnd != null) {
      if (request.endDate != null && request.endDate > scheduleEnd) {
        return badRequest("Allocation end date must not be after the activity schedule end date ($scheduleEnd)")
      }

      if (request.startDate > scheduleEnd) {
        return badRequest("Allocation start date cannot be after the activity schedule end date ($scheduleEnd)")
      }
    }

    return null
  }

  private fun validatePrisonerNotAlreadyAllocated(
    schedule: ActivitiesActivityScheduleDetailed,
    request: PrisonerAllocationRequest,
  ): Response<HmppsMessageResponse?>? {
    val prisonerNumber = request.prisonerNumber
    val allocation = schedule.allocations.find { it.prisonerNumber == prisonerNumber }

    return if (allocation != null && allocation.status != "ENDED") {
      badRequest("Prisoner with $prisonerNumber is already allocated to schedule ${schedule.description}.")
    } else {
      null
    }
  }

  private fun validateExclusionSlots(
    schedule: ActivitiesActivityScheduleDetailed,
    request: PrisonerAllocationRequest,
    scheduleId: Long,
  ): Response<HmppsMessageResponse?>? {
    request.exclusions?.forEach { exclusion ->
      val noMatch =
        schedule.slots.none {
          it.weekNumber == exclusion.weekNumber &&
            it.timeSlot == exclusion.timeSlot &&
            it.getDaysOfWeek().containsAll(exclusion.toDaysOfWeek())
        }
      if (noMatch) {
        return badRequest("No ${exclusion.timeSlot} slots in week number ${exclusion.weekNumber} for schedule $scheduleId")
      }
    }
    return null
  }

  private fun validateWaitingListApplications(
    prisonCode: String,
    prisonerNumber: String,
    filters: ConsumerFilters?,
  ): Response<HmppsMessageResponse?>? {
    val pendingReq = WaitingListSearchRequest(prisonerNumbers = listOf(prisonerNumber), status = listOf("PENDING"))
    val approvedReq = WaitingListSearchRequest(prisonerNumbers = listOf(prisonerNumber), status = listOf("APPROVED"))
    val pending = getWaitingListApplicationsService.execute(prisonCode, pendingReq, filters)
    val approved = getWaitingListApplicationsService.execute(prisonCode, approvedReq, filters)

    if (pending.errors.isNotEmpty()) return Response(data = null, errors = pending.errors)
    if (approved.errors.isNotEmpty()) return Response(data = null, errors = approved.errors)

    if (!pending.data?.content.isNullOrEmpty()) {
      return conflict("Prisoner has a PENDING waiting list application. It must be APPROVED before they can be allocated.")
    }

    if ((approved.data?.content?.size ?: 0) > 1) {
      return conflict("Prisoner has more than one APPROVED waiting list application. A prisoner can only have one approved waiting list application.")
    }

    return null
  }

  private fun badRequest(message: String) = Response<HmppsMessageResponse?>(data = null, errors = listOf(UpstreamApiError(UpstreamApi.ACTIVITIES, UpstreamApiError.Type.BAD_REQUEST, message)))

  private fun conflict(message: String) = Response<HmppsMessageResponse?>(data = null, errors = listOf(UpstreamApiError(UpstreamApi.ACTIVITIES, UpstreamApiError.Type.CONFLICT, message)))

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
