package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.prison

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.assertions.json.shouldContainJsonKeyValue
import io.kotest.matchers.shouldBe
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import software.amazon.awssdk.services.sqs.model.Message
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DeactivateLocationRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DeactivationReason
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.countAllMessagesOnQueue
import java.time.LocalDate

class DeactivateLocationIntegrationTest : IntegrationTestBase() {
  @Autowired
  private lateinit var hmppsQueueService: HmppsQueueService

  internal val testQueue by lazy { hmppsQueueService.findByQueueId("locations") ?: throw RuntimeException("Queue with name locations doesn't exist") }
  internal val testSqsClient by lazy { testQueue.sqsClient }
  internal val testQueueUrl by lazy { testQueue.queueUrl }

  fun getNumberOfMessagesCurrentlyOnQueue(): Int = testSqsClient.countAllMessagesOnQueue(testQueueUrl).get()

  fun checkQueueIsEmpty() {
    await untilCallTo { getNumberOfMessagesCurrentlyOnQueue() } matches { it == 0 }
    getNumberOfMessagesCurrentlyOnQueue().shouldBe(0)
  }

  fun getQueueMessages(): List<Message> = testSqsClient.receiveMessage(ReceiveMessageRequest.builder().queueUrl(testQueueUrl).build()).join().messages()

  private val prisonId = "MDI"
  private val key = "MDI-A-1-001"
  private val path = "/v1/prison/$prisonId/location/$key/deactivate"
  private val deactivateLocationRequest =
    DeactivateLocationRequest(
      deactivationReason = DeactivationReason.DAMAGED,
      deactivationReasonDescription = "Scheduled maintenance",
      proposedReactivationDate = LocalDate.now(),
      planetFmReference = "23423TH/5",
    )

  @BeforeEach
  fun `clear queues`() {
    testSqsClient.purgeQueue(PurgeQueueRequest.builder().queueUrl(testQueueUrl).build())
  }

  @Test
  fun `return the response saying message on queue`() {
    postToApi(path, asJsonString(deactivateLocationRequest))
      .andExpect(status().isOk)
      .andExpect(
        content().json(
          """
          {
            "data": {
              "message": "Deactivate location written to queue"
            }
          }
          """.trimIndent(),
        ),
      )

    val queueMessages = getQueueMessages()
    queueMessages.size.shouldBe(1)

    val messageJson = queueMessages[0].body()
    val expectedMessage = deactivateLocationRequest.toHmppsMessage(locationId = "2475f250-434a-4257-afe7-b911f1773a4d", defaultCn)
    messageJson.shouldContainJsonKeyValue("$.eventType", expectedMessage.eventType.eventTypeCode)
    messageJson.shouldContainJsonKeyValue("$.who", defaultCn)
    val objectMapper = jacksonObjectMapper()
    val messageAttributes = objectMapper.readTree(messageJson).at("/messageAttributes")
    val expectedMessageAttributes = objectMapper.readTree(objectMapper.writeValueAsString(expectedMessage.messageAttributes))
    messageAttributes.shouldBe(expectedMessageAttributes)
  }

  @Test
  fun `return a 404 when prison not in the allowed prisons`() {
    postToApiWithCN(path, asJsonString(deactivateLocationRequest), limitedPrisonsCn)
      .andExpect(status().isNotFound)

    checkQueueIsEmpty()
  }

  @Test
  fun `return a 404 no prisons in filter`() {
    postToApiWithCN(path, asJsonString(deactivateLocationRequest), noPrisonsCn)
      .andExpect(status().isNotFound)

    checkQueueIsEmpty()
  }
}
