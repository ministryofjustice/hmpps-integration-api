package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import com.fasterxml.jackson.databind.ObjectMapper
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.expressionOfInterest.ExpressionInterest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ExpressionOfInterestMessage
import uk.gov.justice.hmpps.sqs.HmppsQueue
import uk.gov.justice.hmpps.sqs.HmppsQueueService

class ExpressionInterestServiceTest : DescribeSpec({
  val mockQueueService = mock<HmppsQueueService>()
  val mockSqsClient = mock<SqsAsyncClient>()
  val mockObjectMapper = mock<ObjectMapper>()

  val eoiQueue =
    mock<HmppsQueue> {
      on { sqsClient } doReturn mockSqsClient
      on { queueUrl } doReturn "https://test-queue-url"
    }

  val service = ExpressionInterestService(mockQueueService, mockObjectMapper)

  beforeTest {
    reset(mockQueueService, mockSqsClient, mockObjectMapper)
    whenever(mockQueueService.findByQueueId("eoi-queue")).thenReturn(eoiQueue)
  }

  describe("sendExpressionOfInterest") {
    it("should send a valid message successfully to SQS") {
      val expressionInterest = ExpressionInterest(jobId = "12345", hmppsId = "H1234")
      val messageBody = """{"jobId":"12345","verifiedHmppsId":"H1234"}"""

      whenever(mockObjectMapper.writeValueAsString(any<ExpressionOfInterestMessage>()))
        .thenReturn(messageBody)

      service.sendExpressionOfInterest(expressionInterest)

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
      val expressionInterest = ExpressionInterest(jobId = "12345", hmppsId = "H1234")

      whenever(mockObjectMapper.writeValueAsString(any<ExpressionOfInterestMessage>()))
        .thenReturn("""{"jobId":"12345","verifiedHmppsId":"H1234"}""")

      whenever(mockSqsClient.sendMessage(any<SendMessageRequest>()))
        .thenThrow(RuntimeException("Failed to send message to SQS"))

      val exception =
        shouldThrow<MessageFailedException> {
          service.sendExpressionOfInterest(expressionInterest)
        }

      exception.message shouldBe "Failed to send message to SQS"
    }
  }
})
