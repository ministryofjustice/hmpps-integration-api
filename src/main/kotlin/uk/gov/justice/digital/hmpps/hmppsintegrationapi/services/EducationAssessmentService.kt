package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.validation.ValidationException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.MessageFailedException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.EducationAssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.EducationAssessmentStatusChangeRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsMessage
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsMessageEventType
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsMessageResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.hmpps.sqs.HmppsQueue
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.eventTypeMessageAttributes
import java.time.LocalDate

@Service
class EducationAssessmentService(
  private val getPersonService: GetPersonService,
  private val hmppsQueueService: HmppsQueueService,
  private val objectMapper: ObjectMapper,
) {
  private val assessmentEventsQueue by lazy { hmppsQueueService.findByQueueId("assessmentevents") as HmppsQueue }
  private val assessmentEventsQueueSqsClient by lazy { assessmentEventsQueue.sqsClient }
  private val assessmentEventsQueueUrl by lazy { assessmentEventsQueue.queueUrl }

  companion object {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun sendEducationAssessmentEvent(
    hmppsId: String,
    request: EducationAssessmentStatusChangeRequest,
  ): Response<HmppsMessageResponse> {
    val personResponse = getPersonService.getNomisNumber(hmppsId = hmppsId)

    if (personResponse.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      logger.debug("AssessmentEvents: Could not find nomis number for hmppsId: $hmppsId")
      throw EntityNotFoundException("Could not find person with id: $hmppsId")
    }

    if (personResponse.hasError(UpstreamApiError.Type.BAD_REQUEST)) {
      logger.debug("AssessmentEvents: Invalid hmppsId: $hmppsId")
      throw ValidationException("Invalid HMPPS ID: $hmppsId")
    }

    val nomisNumber = personResponse.data?.nomisNumber ?: run { throw ValidationException("Invalid HMPPS ID: $hmppsId") }
    val assessmentEvent =
      AssessmentEvent(
        prisonNumber = nomisNumber,
        status = request.status,
        statusChangeDate = request.statusChangeDate,
        detailUrl = request.detailUrl?.toString(),
        requestId = request.requestId,
      )

    val eventType = HmppsMessageEventType.EDUCATION_ASSESSMENT_EVENT_CREATED
    try {
      val hmppsMessage =
        objectMapper.writeValueAsString(
          HmppsMessage(
            eventType = eventType,
            messageAttributes =
              with(assessmentEvent) {
                mapOf(
                  "prisonNumber" to prisonNumber,
                  "status" to status,
                  "statusChangeDate" to statusChangeDate,
                  "detailUrl" to detailUrl,
                  "requestId" to request.requestId,
                )
              },
          ),
        )

      assessmentEventsQueueSqsClient.sendMessage(
        SendMessageRequest
          .builder()
          .queueUrl(assessmentEventsQueueUrl)
          .messageBody(hmppsMessage)
          .eventTypeMessageAttributes(eventType.type)
          .build(),
      )
    } catch (e: Exception) {
      throw MessageFailedException("Failed to send assessment event message to SQS", e)
    }
    return Response(HmppsMessageResponse(message = "Education assessment event written to queue"))
  }
}

data class AssessmentEvent(
  val prisonNumber: String,
  val status: EducationAssessmentStatus,
  val statusChangeDate: LocalDate,
  val detailUrl: String?,
  val requestId: String,
)
