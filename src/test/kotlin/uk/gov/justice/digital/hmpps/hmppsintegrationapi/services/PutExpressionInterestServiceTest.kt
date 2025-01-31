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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ExpressionOfInterestMessage
import uk.gov.justice.hmpps.sqs.HmppsQueue
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import kotlin.test.assertEquals

class PutExpressionInterestServiceTest :
  DescribeSpec({
    val mockQueueService = mock<HmppsQueueService>()
    val mockSqsClient = mock<SqsAsyncClient>()
    val mockObjectMapper = mock<ObjectMapper>()

    val eoiQueue =
      mock<HmppsQueue> {
        on { sqsClient } doReturn mockSqsClient
        on { queueUrl } doReturn "https://test-queue-url"
      }

    val service = PutExpressionInterestService(mockQueueService, mockObjectMapper)

    beforeTest {
      reset(mockQueueService, mockSqsClient, mockObjectMapper)
      whenever(mockQueueService.findByQueueId("jobsboardintegration")).thenReturn(eoiQueue)
    }

    describe("sendExpressionOfInterest") {
      it("should send a valid message successfully to SQS") {
        val expressionOfInterest = ExpressionOfInterest(jobId = "12345", prisonNumber = "H1234")
        val expectedMessage =
          ExpressionOfInterestMessage(
            messageId = "1",
            jobId = "12345",
            prisonNumber = "H1234",
          )
        val messageBody = objectMapper.writeValueAsString(expectedMessage)

        whenever(mockObjectMapper.writeValueAsString(any<ExpressionOfInterestMessage>()))
          .thenReturn(messageBody)

        service.sendExpressionOfInterest(expressionOfInterest)

        verify(mockSqsClient).sendMessage(
          argThat<SendMessageRequest> { request: SendMessageRequest? ->
            (
              request?.queueUrl() == "https://test-queue-url" &&
                request.messageBody() == messageBody
            )
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
          ExpressionOfInterestMessage(
            messageId = "1",
            jobId = "12345",
            prisonNumber = "H1234",
          )

        val serializedJson = objectMapper.writeValueAsString(expectedMessage)

        val deserializedMap: Map<String, Any?> = objectMapper.readValue(serializedJson)
        val eventType = deserializedMap["eventType"]

        assert(deserializedMap.containsKey("messageId"))
        assert(deserializedMap.containsKey("jobId"))
        assert(deserializedMap.containsKey("prisonNumber"))
        assert(deserializedMap.containsKey("eventType"))
        assertEquals(
          expected = ExpressionOfInterestMessage.EventType.EXPRESSION_OF_INTEREST_MESSAGE_CREATED.name,
          actual = eventType,
        )
      }
    }
  })
