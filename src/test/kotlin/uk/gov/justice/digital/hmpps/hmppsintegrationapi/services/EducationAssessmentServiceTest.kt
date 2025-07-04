package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import jakarta.validation.ValidationException
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClientResponseException
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.MessageFailedException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.MockMvcExtensions
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.EducationAndWorkPlanGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.education.EducationAssessmentSummaryResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.EducationAssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.EducationAssessmentStatusChangeRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsMessage
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsMessageEventType
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.NomisNumber
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.hmpps.sqs.HmppsQueue
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import java.net.URI
import java.time.LocalDate
import java.util.UUID
import kotlin.test.assertEquals

class EducationAssessmentServiceTest :
  DescribeSpec({
    val mockGetPersonService = mock<GetPersonService>()
    val mockQueueService = mock<HmppsQueueService>()
    val mockObjectMapper = mock<ObjectMapper>()
    val mockEducationAndWorkPlanGateway = mock<EducationAndWorkPlanGateway>()
    val mockSqsClient = mock<SqsAsyncClient>()

    val assessmentEventsQueue =
      mock<HmppsQueue> {
        on { sqsClient } doReturn mockSqsClient
        on { queueUrl } doReturn "https://test-queue-url"
      }

    val aValidEducationAssessmentRequest =
      EducationAssessmentStatusChangeRequest(
        status = EducationAssessmentStatus.ALL_RELEVANT_ASSESSMENTS_COMPLETE,
        statusChangeDate = LocalDate.now(),
        detailUrl = URI.create("https://detail-url").toURL(),
        requestId = UUID.randomUUID().toString(),
      )

    val queId = "assessmentevents"
    val service =
      EducationAssessmentService(
        mockGetPersonService,
        mockQueueService,
        mockObjectMapper,
        mockEducationAndWorkPlanGateway,
      )

    beforeTest {
      reset(mockQueueService, mockSqsClient, mockObjectMapper)
      whenever(mockQueueService.findByQueueId(queId)).thenReturn(assessmentEventsQueue)
    }

    describe("getEducationAssessmentStatus") {
      val validHmppsId = "H1234"

      beforeTest {
        whenever(mockGetPersonService.getNomisNumber(validHmppsId)).thenReturn(Response(NomisNumber(validHmppsId)))
      }

      it("should return a education assessment summary if the api calls are successful") {
        val gatewayResponse = Response<EducationAssessmentSummaryResponse?>(EducationAssessmentSummaryResponse(true))
        whenever(mockEducationAndWorkPlanGateway.getEducationAssessmentSummary(any())).thenReturn(gatewayResponse)

        service.getEducationAssessmentStatus(validHmppsId).shouldBe(gatewayResponse)
      }

      it("should throw a EntityNotFoundException for a upstream entity not found error") {
        val invalidHmppsId = "X0000"
        val upstreamApiError = UpstreamApiError(UpstreamApi.EDUCATION_AND_WORK_PLAN, UpstreamApiError.Type.ENTITY_NOT_FOUND)
        whenever(mockGetPersonService.getNomisNumber(invalidHmppsId)).thenReturn(Response(null, listOf<UpstreamApiError>(upstreamApiError)))

        shouldThrow<EntityNotFoundException> {
          service.getEducationAssessmentStatus(invalidHmppsId)
        }
      }

      it("should throw a ValidationException for a upstream bad request error") {
        val invalidHmppsId = "X0000"
        val upstreamApiError = UpstreamApiError(UpstreamApi.EDUCATION_AND_WORK_PLAN, UpstreamApiError.Type.BAD_REQUEST)
        whenever(mockGetPersonService.getNomisNumber(invalidHmppsId)).thenReturn(Response(null, listOf<UpstreamApiError>(upstreamApiError)))

        shouldThrow<ValidationException> {
          service.getEducationAssessmentStatus(invalidHmppsId)
        }
      }

      it("should throw a ValidationException if getNomisNumber does not return a nomis number") {
        val invalidHmppsId = "X0000"
        whenever(mockGetPersonService.getNomisNumber(invalidHmppsId)).thenReturn(Response(null, listOf()))

        shouldThrow<ValidationException> {
          service.getEducationAssessmentStatus(invalidHmppsId)
        }
      }

      it("should allow any WebClientResponse exceptions to bubble up") {
        val exception = WebClientResponseException(403, "Forbidden", null, null, null)
        whenever(mockEducationAndWorkPlanGateway.getEducationAssessmentSummary(any())).thenThrow(exception)

        val response =
          shouldThrow<WebClientResponseException> {
            service.getEducationAssessmentStatus(validHmppsId)
          }
        response.statusCode.shouldBe(HttpStatus.FORBIDDEN)
      }
    }

    describe("sendAssessmentEvent") {
      beforeTest {
        "H1234".let { whenever(mockGetPersonService.getNomisNumber(it)).thenReturn(Response(NomisNumber(it))) }
      }

      it("should send a valid message successfully to SQS") {
        val hmppsId = "H1234"
        val messageBody =
          """{"messageId":"1","eventType":"EducationAssessmentEventCreated","messageAttributes":
          |{"requestId":"${aValidEducationAssessmentRequest.requestId}",
          |"prisonNumber":"H1234",
          |"detailUrl":"https://detail-url",
          |"statusChangeDate":"${aValidEducationAssessmentRequest.statusChangeDate}"}}
          |
          """.trimMargin()

        whenever(mockObjectMapper.writeValueAsString(any<HmppsMessage>()))
          .thenReturn(messageBody)

        service.sendEducationAssessmentEvent(hmppsId, aValidEducationAssessmentRequest)

        verify(mockSqsClient).sendMessage(
          argThat<SendMessageRequest> { request: SendMessageRequest? ->
            request?.queueUrl() == "https://test-queue-url" &&
              request.messageBody() == messageBody
          },
        )
      }

      it("should throw MessageFailedException when SQS fails") {
        val hmppsId = "H1234"

        whenever(mockSqsClient.sendMessage(any<SendMessageRequest>()))
          .thenThrow(RuntimeException("Failed to send message to SQS"))

        val exception =
          shouldThrow<MessageFailedException> {
            service.sendEducationAssessmentEvent(hmppsId, aValidEducationAssessmentRequest)
          }

        exception.message shouldBe "Failed to send assessment event message to SQS"
      }

      it("should serialize EducationAssessmentEventMessage with correct keys") {
        val expectedMessage =
          HmppsMessage(
            messageId = "1",
            eventType = HmppsMessageEventType.EDUCATION_ASSESSMENT_EVENT_CREATED,
            messageAttributes =
              mapOf(
                "prisonNumber" to "H1234",
                "status" to aValidEducationAssessmentRequest.status,
                "statusChangeDate" to aValidEducationAssessmentRequest.statusChangeDate.toString(),
                "detailUrl" to aValidEducationAssessmentRequest.detailUrl,
                "requestId" to aValidEducationAssessmentRequest.requestId,
              ),
          )

        val serializedJson = MockMvcExtensions.objectMapper.writeValueAsString(expectedMessage)

        val deserializedMap: Map<String, Any?> = MockMvcExtensions.objectMapper.readValue(serializedJson)
        val eventType = deserializedMap["eventType"]
        assert(deserializedMap.containsKey("messageId"))
        assert(deserializedMap.containsKey("messageAttributes"))
        assert(deserializedMap.containsKey("eventType"))
        assertEquals(
          expected = "EducationAssessmentEventCreated",
          actual = eventType,
        )

        val messageAttributes = deserializedMap["messageAttributes"] as? Map<*, *>
        messageAttributes?.containsKey("prisonNumber")?.let { assert(it) }
      }

      it("should serialize EducationAssessmentEventMessage with EducationAssessmentEventCreated type") {
        val hmppsId = "H1234"
        val expectedMessage =
          HmppsMessage(
            messageId = "1",
            eventType = HmppsMessageEventType.EDUCATION_ASSESSMENT_EVENT_CREATED,
            messageAttributes =
              mapOf(
                "prisonNumber" to "H1234",
                "status" to aValidEducationAssessmentRequest.status,
                "statusChangeDate" to aValidEducationAssessmentRequest.statusChangeDate.toString(),
                "detailUrl" to aValidEducationAssessmentRequest.detailUrl,
                "requestId" to aValidEducationAssessmentRequest.requestId,
              ),
          )

        val expectedMessageBody = MockMvcExtensions.objectMapper.writeValueAsString(expectedMessage)
        val deserializedMap: Map<String, Any?> = MockMvcExtensions.objectMapper.readValue(expectedMessageBody)
        val eventType = deserializedMap["eventType"]

        assertEquals(
          expected = "EducationAssessmentEventCreated",
          actual = eventType,
        )

        whenever(mockObjectMapper.writeValueAsString(any<HmppsMessage>()))
          .thenReturn(expectedMessageBody)

        service.sendEducationAssessmentEvent(hmppsId, aValidEducationAssessmentRequest)

        verify(mockSqsClient).sendMessage(
          argThat<SendMessageRequest> { request ->
            request?.queueUrl() == "https://test-queue-url" &&
              MockMvcExtensions.objectMapper.readTree(request.messageBody()) ==
              MockMvcExtensions.objectMapper.readTree(
                expectedMessageBody,
              )
          },
        )
      }
    }

    describe("sendExpressionOfInterest, with errors at HMPPS ID translation") {
      val validHmppsId = "AABCD1ABC"
      val invalidHmppsId = "INVALID_ID"

      it("should throw EntityNotFoundException. if ENTITY_NOT_FOUND error occurs") {
        val hmppsId = validHmppsId
        val notFoundResponse = errorResponseNomisNumber(UpstreamApiError.Type.ENTITY_NOT_FOUND, "Entity not found")
        whenever(mockGetPersonService.getNomisNumber(hmppsId)).thenReturn(notFoundResponse)

        val exception = shouldThrow<EntityNotFoundException> { service.sendEducationAssessmentEvent(hmppsId, aValidEducationAssessmentRequest) }

        assertEquals("Could not find person with id: $hmppsId", exception.message)
      }

      it("should throw ValidationException if an invalid hmppsId is provided") {
        val hmppsId = invalidHmppsId
        val invalidIdBadRequestResponse =
          errorResponseNomisNumber(UpstreamApiError.Type.BAD_REQUEST, "Invalid HMPPS ID")
        whenever(mockGetPersonService.getNomisNumber(hmppsId)).thenReturn(invalidIdBadRequestResponse)

        val exception = shouldThrow<ValidationException> { service.sendEducationAssessmentEvent(hmppsId, aValidEducationAssessmentRequest) }

        assertEquals("Invalid HMPPS ID: $hmppsId", exception.message)
      }
    }
  })

private fun errorResponseNomisNumber(
  errorType: UpstreamApiError.Type,
  errorDescription: String,
) = Response<NomisNumber?>(
  data = null,
  errors = listOf(UpstreamApiError(type = errorType, description = errorDescription, causedBy = UpstreamApi.PRISONER_OFFENDER_SEARCH)),
)
