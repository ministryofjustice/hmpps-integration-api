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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.toHmppsMessage
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.hmpps.sqs.HmppsQueue
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.eventTypeMessageAttributes

@Service
class ActivitiesQueueService(
  @Autowired private val hmppsQueueService: HmppsQueueService,
  @Autowired private val objectMapper: ObjectMapper,
  @Autowired private val consumerPrisonAccessService: ConsumerPrisonAccessService,
  @Autowired private val getAttendanceByIdService: GetAttendanceByIdService,
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
