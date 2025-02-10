package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.MessageFailedException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.MockMvcExtensions.objectMapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ExpressionOfInterest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsMessage
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsMessageEventType
import uk.gov.justice.hmpps.sqs.HmppsQueue
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import kotlin.test.assertEquals

class PutExpressionInterestServiceTest :
  DescribeSpec({
    val mockQueueService = mock<HmppsQueueService>()
    val mockObjectMapper = mock<ObjectMapper>()
    val mockSqsClient = mock<SqsAsyncClient>()

    val eoiQueue =
      mock<HmppsQueue> {
        on { sqsClient } doReturn mockSqsClient
        on { queueUrl } doReturn "https://test-queue-url"
      }

    val queId = "jobsboardintegration"
    val service = PutExpressionInterestService(mockQueueService, mockObjectMapper)

    beforeTest {
      reset(mockQueueService, mockSqsClient, mockObjectMapper)
      whenever(mockQueueService.findByQueueId(queId)).thenReturn(eoiQueue)
    }

    describe("sendExpressionOfInterest") {
      it("should send a valid message successfully to SQS") {
        val expressionOfInterest = ExpressionOfInterest(jobId = "12345", prisonNumber = "H1234")
        val messageBody = """{"messageId":"1","eventType":"ExpressionOfInterestCreated","messageAttributes":{"jobId":"12345","prisonNumber":"H1234"}}"""

        whenever(mockObjectMapper.writeValueAsString(any<HmppsMessage>()))
          .thenReturn(messageBody)

        service.sendExpressionOfInterest(expressionOfInterest)

        verify(mockSqsClient).sendMessage(
          argThat<SendMessageRequest> { request: SendMessageRequest? ->
            request?.queueUrl() == "https://test-queue-url" &&
              request.messageBody() == messageBody
          },
        )
      }

      it("should throw MessageFailedException when SQS fails") {
        val expressionInterest = ExpressionOfInterest(jobId = "12345", prisonNumber = "H1234")

        whenever(mockSqsClient.sendMessage(any<SendMessageRequest>()))
          .thenThrow(RuntimeException("Failed to send message to SQS"))

        val exception =
          shouldThrow<MessageFailedException> {
            service.sendExpressionOfInterest(expressionInterest)
          }

        exception.message shouldBe "Failed to send message to SQS"
      }

      it("should serialize ExpressionOfInterestMessage with correct keys") {
        val expectedMessage =
          HmppsMessage(
            messageId = "1",
            eventType = HmppsMessageEventType.EXPRESSION_OF_INTEREST_CREATED,
            messageAttributes =
              mapOf(
                "jobId" to "12345",
                "prisonNumber" to "H1234",
              ),
          )

        val serializedJson = objectMapper.writeValueAsString(expectedMessage)

        val deserializedMap: Map<String, Any?> = objectMapper.readValue(serializedJson)
        val eventType = deserializedMap["eventType"]
        assert(deserializedMap.containsKey("messageId"))
        assert(deserializedMap.containsKey("messageAttributes"))
        assert(deserializedMap.containsKey("eventType"))
        assertEquals(
          expected = "ExpressionOfInterestCreated",
          actual = eventType,
        )

        val messageAttributes = deserializedMap["messageAttributes"] as? Map<*, *>
        messageAttributes?.containsKey("jobId")?.let { assert(it) }
        messageAttributes?.containsKey("prisonNumber")?.let { assert(it) }
      }

      it("should serialize ExpressionOfInterestMessage with ExpressionOfInterestCreated type") {
        val expressionOfInterest = ExpressionOfInterest(jobId = "12345", prisonNumber = "H1234")
        val expectedMessage =
          HmppsMessage(
            messageId = "1",
            eventType = HmppsMessageEventType.EXPRESSION_OF_INTEREST_CREATED,
            messageAttributes =
              mapOf(
                "jobId" to "12345",
                "prisonNumber" to "H1234",
              ),
          )

        val expectedMessageBody = objectMapper.writeValueAsString(expectedMessage)
        val deserializedMap: Map<String, Any?> = objectMapper.readValue(expectedMessageBody)
        val eventType = deserializedMap["eventType"]

        assertEquals(
          expected = "ExpressionOfInterestCreated",
          actual = eventType,
        )

        whenever(mockObjectMapper.writeValueAsString(any<HmppsMessage>()))
          .thenReturn(expectedMessageBody)

        service.sendExpressionOfInterest(expressionOfInterest)

        verify(mockSqsClient).sendMessage(
          argThat<SendMessageRequest> { request ->
            request?.queueUrl() == "https://test-queue-url" &&
              objectMapper.readTree(request.messageBody()) == objectMapper.readTree(expectedMessageBody)
          },
        )
      }
    }
  })
