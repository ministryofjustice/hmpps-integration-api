package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.MessageFailedException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CourseCompletion
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CourseDetails
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.EducationCourseCompletionRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonDetails
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService
import uk.gov.justice.hmpps.sqs.HmppsQueue
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import java.time.LocalDate

class EducationCourseServiceTest :
  DescribeSpec({
    val mockAuditService = mock<AuditService>()
    val mockQueueService = mock<HmppsQueueService>()
    val mockObjectMapper = mock<ObjectMapper>()
    val mockSqsClient = mock<SqsAsyncClient>()

    val educationStatusEventsQueue =
      mock<HmppsQueue> {
        on { sqsClient } doReturn mockSqsClient
        on { queueUrl } doReturn "https://test-queue-url"
      }

    val service =
      EducationCourseCompletionService(
        mockAuditService,
        mockQueueService,
        mockObjectMapper,
      )

    val request =
      EducationCourseCompletionRequest(
        courseCompletion =
          CourseCompletion(
            externalReference = "CC123",
            person =
              PersonDetails(
                firstName = "John",
                lastName = "Doe",
                dateOfBirth = LocalDate.of(1990, 1, 1),
                region = "London",
                email = "john.doe@example.com",
              ),
            course =
              CourseDetails(
                courseName = "Test Course",
                courseType = "Test course type",
                provider = "Moodle",
                completionDate = LocalDate.of(2024, 1, 15),
                status = "Completed",
                totalTimeMinutes = 150,
                attempts = 1,
                expectedTimeMinutes = 120,
              ),
          ),
      )

    beforeTest {
      reset(mockAuditService, mockQueueService, mockSqsClient, mockObjectMapper)
      whenever(mockQueueService.findByQueueId("educationcoursecompletionevents")).thenReturn(educationStatusEventsQueue)
    }

    describe("recordCourseCompletion") {
      it("calls the audit service") {
        service.recordCourseCompletion(request)

        verify(mockAuditService).createEvent(
          "RECORD_COURSE_COMPLETION",
          mapOf(
            "externalReference" to "CC123",
            "courseName" to "Test Course",
            "personEmail" to "john.doe@example.com",
          ),
        )
      }

      it("sends a message to the educationstatusevents queue") {
        whenever(mockObjectMapper.writeValueAsString(any())).thenReturn("{\"message\":\"test\"}")

        service.recordCourseCompletion(request)

        verify(mockSqsClient).sendMessage(
          argThat<SendMessageRequest> {
            queueUrl() == "https://test-queue-url" && messageBody() == "{\"message\":\"test\"}"
          },
        )
      }

      it("throws MessageFailedException if sending to SQS fails") {
        whenever(mockObjectMapper.writeValueAsString(any())).thenReturn("{\"message\":\"test\"}")
        whenever(mockSqsClient.sendMessage(any<SendMessageRequest>())).thenThrow(RuntimeException("SQS error"))

        shouldThrow<MessageFailedException> {
          service.recordCourseCompletion(request)
        }
      }
    }

    describe("recordCourseCompletion audit behavior") {
      it("creates audit event when message is successfully sent to SQS") {
        whenever(mockObjectMapper.writeValueAsString(any())).thenReturn("{\"message\":\"test\"}")

        service.recordCourseCompletion(request)

        verify(mockAuditService).createEvent(
          "RECORD_COURSE_COMPLETION",
          mapOf(
            "externalReference" to "CC123",
            "courseName" to "Test Course",
            "personEmail" to "john.doe@example.com",
          ),
        )
      }

      it("does not create audit event when SQS message sending fails") {
        whenever(mockObjectMapper.writeValueAsString(any())).thenReturn("{\"message\":\"test\"}")
        whenever(mockSqsClient.sendMessage(any<SendMessageRequest>())).thenThrow(RuntimeException("SQS error"))

        shouldThrow<MessageFailedException> {
          service.recordCourseCompletion(request)
        }

        verify(mockAuditService, never()).createEvent(any(), any())
      }

      it("handles audit service exception gracefully without failing the main operation") {
        whenever(mockObjectMapper.writeValueAsString(any())).thenReturn("{\"message\":\"test\"}")
        whenever(mockAuditService.createEvent(any(), any())).thenThrow(RuntimeException("Audit service error"))

        val response = service.recordCourseCompletion(request)

        response shouldNotBe null
        response.data.message shouldContain "Education course completion event written to queue"

        // Verify SQS message was still sent
        verify(mockSqsClient).sendMessage(any<SendMessageRequest>())
      }

      it("audit event contains all required fields") {
        whenever(mockObjectMapper.writeValueAsString(any())).thenReturn("{\"message\":\"test\"}")

        service.recordCourseCompletion(request)

        verify(mockAuditService).createEvent(
          eq("RECORD_COURSE_COMPLETION"),
          argThat<Map<String, String>> { auditData ->
            auditData["externalReference"] == "CC123" &&
              auditData["courseName"] == "Test Course" &&
              auditData["personEmail"] == "john.doe@example.com"
          },
        )
      }
    }
  })
