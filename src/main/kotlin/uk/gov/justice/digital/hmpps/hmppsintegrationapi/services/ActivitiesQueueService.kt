package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.MessageFailedException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.AttendanceUpdateRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsMessage
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsMessageResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonerDeallocationRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
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
