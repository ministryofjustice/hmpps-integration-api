package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.MessageFailedException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.EducationCourseCompletionRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsMessage
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsMessageEventType
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsMessageResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService
import uk.gov.justice.hmpps.sqs.HmppsQueue
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.eventTypeMessageAttributes

@Service
class EducationCourseCompletionService(
  private val auditService: AuditService,
  private val hmppsQueueService: HmppsQueueService,
  private val objectMapper: ObjectMapper,
) {
  private val educationCourseCompletionEventsQueue by lazy { hmppsQueueService.findByQueueId("educationcoursecompletionevents") as HmppsQueue }
  private val educationCourseCompletionEventsQueueSqsClient by lazy { educationCourseCompletionEventsQueue.sqsClient }
  private val educationCourseCompletionEventsQueueUrl by lazy { educationCourseCompletionEventsQueue.queueUrl }

  companion object {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun recordCourseCompletion(request: EducationCourseCompletionRequest): Response<HmppsMessageResponse> {
    val courseCompletion = request.courseCompletion
    val person = courseCompletion.person
    val course = courseCompletion.course
    val eventType = HmppsMessageEventType.EDUCATION_COURSE_COMPLETION_CREATED

    val hmppsMessage =
      try {
        objectMapper.writeValueAsString(
          HmppsMessage(
            eventType = eventType,
            messageAttributes =
              mapOf(
                "crn" to person.crn,
                "firstName" to person.firstName,
                "lastName" to person.lastName,
                "dateOfBirth" to person.dateOfBirth.toString(),
                "region" to person.region,
                "email" to person.email,
                "courseName" to course.courseName,
                "courseType" to course.courseType,
                "provider" to course.provider,
                "completionDateTime" to course.completionDateTime.toString(),
                "status" to course.status,
                "totalTime" to course.totalTime,
                "attempts" to course.attempts?.toString().orEmpty(),
                "expectedMinutes" to course.expectedMinutes.toString(),
                "externalReference" to courseCompletion.externalReference,
              ),
          ),
        )
      } catch (e: Exception) {
        logger.error("Failed to build education course completion event message: ${eventType.type}", e)
        throw MessageFailedException("Failed to build education course completion event message", e)
      }

    try {
      educationCourseCompletionEventsQueueSqsClient.sendMessage(
        SendMessageRequest
          .builder()
          .queueUrl(educationCourseCompletionEventsQueueUrl)
          .messageBody(hmppsMessage)
          .eventTypeMessageAttributes(eventType.type)
          .build(),
      )
    } catch (e: Exception) {
      logger.error("Failed to send education course completion event to SQS: ${eventType.type}, queueURL: $educationCourseCompletionEventsQueueUrl", e)
      throw MessageFailedException("Failed to send education course completion event message to SQS", e)
    }

    try {
      auditService.createEvent(
        "RECORD_COURSE_COMPLETION",
        mapOf(
          "externalReference" to courseCompletion.externalReference,
          "crn" to person.crn,
          "courseName" to course.courseName,
          "personEmail" to person.email,
        ),
      )
    } catch (e: Exception) {
      logger.error("Failed to create audit event for education course completion: ${eventType.type}", e)
    }

    return Response(HmppsMessageResponse(message = "Education course completion event written to queue"))
  }
}
