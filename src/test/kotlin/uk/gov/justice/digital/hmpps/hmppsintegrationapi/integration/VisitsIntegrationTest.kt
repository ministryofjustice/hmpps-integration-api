package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.assertions.json.shouldContainJsonKeyValue
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import software.amazon.awssdk.services.sqs.model.Message
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CancelOutcome
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CancelVisitRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CreateVisitRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.OutcomeStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.VisitContact
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.VisitNotes
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.VisitRestriction
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.VisitType
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Visitor
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.VisitorSupport
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import java.time.LocalDateTime

class VisitsIntegrationTest : IntegrationTestBase() {
  @Autowired
  protected lateinit var hmppsQueueService: HmppsQueueService

  internal val testQueue by lazy { hmppsQueueService.findByQueueId("visits") ?: throw RuntimeException("Queue with name visits doesn't exist") }
  internal val testSqsClient by lazy { testQueue.sqsClient }
  internal val testQueueUrl by lazy { testQueue.queueUrl }

  fun getQueueMessages(): List<Message> = testSqsClient.receiveMessage(ReceiveMessageRequest.builder().queueUrl(testQueueUrl).build()).join().messages()

  @BeforeEach
  fun `clear queues`() {
    testSqsClient.purgeQueue(PurgeQueueRequest.builder().queueUrl(testQueueUrl).build())
  }

  @DisplayName("GET /v1/visit/{visitReference}")
  @Nested
  inner class GetVisitByReference {
    private val visitReference = "123456"

    @Test
    fun `gets the visit detail`() {
      callApi("/v1/visit/$visitReference")
        .andExpect(status().isOk)
        .andExpect(
          content().json(
            """
            {
              "data": {
                  "prisonerId": "AF34567G",
                  "prisonId": "MDI",
                  "prisonName": "Moorland (HMP & YOI)",
                  "visitRoom": "Visits Main Hall",
                  "visitType": "SOCIAL",
                  "visitStatus": "BOOKED",
                  "outcomeStatus": "ADMINISTRATIVE_CANCELLATION",
                  "visitRestriction": "OPEN",
                  "startTimestamp": "2018-12-01T13:45:00",
                  "endTimestamp": "2018-12-01T13:45:00",
                  "createdTimestamp": "2018-12-01T13:45:00",
                  "modifiedTimestamp": "2018-12-01T13:45:00",
                  "firstBookedDateTime": "2018-12-01T13:45:00",
                  "visitors": [{ "contactId": 1234, "visitContact": true}],
                  "visitNotes": [{ "type": "VISITOR_CONCERN", "text": "Visitor is concerned that his mother in-law is coming!"}],
                  "visitContact": {"name": "John Smith", "telephone": "01234 567890", "email": "email@example.com"},
                  "visitorSupport": {"description": "visually impaired assistance"}
              }
            }
      """,
          ),
        )
    }

    @Test
    fun `return a 404 when prison not in filter`() {
      callApiWithCN("/v1/visit/$visitReference", limitedPrisonsCn)
        .andExpect(status().isNotFound)
    }

    @Test
    fun `return a 404 when no prisons in filter`() {
      callApiWithCN("/v1/visit/$visitReference", noPrisonsCn)
        .andExpect(status().isNotFound)
    }
  }

  @DisplayName("POST /v1/visit")
  @Nested
  inner class PostVisit {
    private val timestamp = "2020-12-04T10:42:43"
    private val prisonerId = "A1234AB"

    private fun getCreateVisitRequest(prisonerId: String) =
      CreateVisitRequest(
        prisonerId = prisonerId,
        prisonId = "MDI",
        clientVisitReference = "123456",
        visitRoom = "A1",
        visitType = VisitType.SOCIAL,
        visitRestriction = VisitRestriction.OPEN,
        startTimestamp = LocalDateTime.parse(timestamp),
        endTimestamp = LocalDateTime.parse(timestamp),
        visitNotes = listOf(VisitNotes(type = "VISITOR_CONCERN", text = "Visitor is concerned their mother in law is coming!")),
        visitContact = VisitContact(name = "John Smith", telephone = "0987654321", email = "john.smith@example.com"),
        createDateTime = LocalDateTime.parse(timestamp),
        visitors = setOf(Visitor(nomisPersonId = 3L, visitContact = true)),
        visitorSupport = VisitorSupport(description = "Visually impaired assistance"),
      )

    @Test
    fun `post the visit, get back a message response and find a message on the queue`() {
      val createVisitRequest = getCreateVisitRequest(prisonerId)
      val requestBody = asJsonString(createVisitRequest)

      postToApi("/v1/visit", requestBody)
        .andExpect(status().isOk)
        .andExpect(
          content().json(
            """
            {
              "data": {
                  "message": "Visit creation written to queue"
              }
            }
            """,
          ),
        )

      val queueMessages = getQueueMessages()
      queueMessages.size.shouldBe(1)

      val messageJson = queueMessages[0].body()
      val expectedMessage = createVisitRequest.toHmppsMessage(defaultCn)
      messageJson.shouldContainJsonKeyValue("$.eventType", expectedMessage.eventType.eventTypeCode)
      messageJson.shouldContainJsonKeyValue("$.who", defaultCn)
      val objectMapper = jacksonObjectMapper()
      val messageAttributes = objectMapper.readTree(messageJson).at("/messageAttributes")
      val expectedMessageAttributes = objectMapper.readTree(objectMapper.writeValueAsString(expectedMessage.messageAttributes))
      messageAttributes.shouldBe(expectedMessageAttributes)
    }

    @Test
    fun `return a 400 when prisoner ID not valid`() {
      val createVisitRequest = getCreateVisitRequest("INVALID_PRISON_ID")
      val requestBody = asJsonString(createVisitRequest)

      postToApiWithCN("/v1/visit", requestBody, limitedPrisonsCn)
        .andExpect(status().isBadRequest)
    }

    @Test
    fun `return a 404 when prison not in filter`() {
      val createVisitRequest = getCreateVisitRequest(prisonerId)
      val requestBody = asJsonString(createVisitRequest)

      postToApiWithCN("/v1/visit", requestBody, limitedPrisonsCn)
        .andExpect(status().isNotFound)
    }

    @Test
    fun `return a 404 when no prisons in filter`() {
      val createVisitRequest = getCreateVisitRequest(prisonerId)
      val requestBody = asJsonString(createVisitRequest)

      postToApiWithCN("/v1/visit", requestBody, noPrisonsCn)
        .andExpect(status().isNotFound)
    }
  }

  @DisplayName("POST /v1/visit/{visitReference}/cancel")
  @Nested
  inner class PostCancelVisit {
    private val visitReference = "123456"
    private val path = "/v1/visit/$visitReference/cancel"
    private val cancelVisitRequest =
      CancelVisitRequest(
        cancelOutcome =
          CancelOutcome(
            outcomeStatus = OutcomeStatus.VISIT_ORDER_CANCELLED,
            text = "visit order cancelled",
          ),
        actionedBy = "test-consumer",
      )

    @Test
    fun `post the visit cancellation, get back a message response and find a message on the queue`() {
      val requestBody = asJsonString(cancelVisitRequest)

      postToApi(path, requestBody)
        .andExpect(status().isOk)
        .andExpect(
          content().json(
            """
            {
              "data": {
                  "message": "Visit cancellation written to queue"
              }
            }
            """,
          ),
        )

      val queueMessages = getQueueMessages()
      queueMessages.size.shouldBe(1)

      val messageJson = queueMessages[0].body()
      val expectedMessage = cancelVisitRequest.toHmppsMessage(defaultCn, visitReference)
      messageJson.shouldContainJsonKeyValue("$.eventType", expectedMessage.eventType.eventTypeCode)
      messageJson.shouldContainJsonKeyValue("$.who", defaultCn)
      val objectMapper = jacksonObjectMapper()
      val messageAttributes = objectMapper.readTree(messageJson).at("/messageAttributes")
      val expectedMessageAttributes = objectMapper.readTree(objectMapper.writeValueAsString(expectedMessage.messageAttributes))
      messageAttributes.shouldBe(expectedMessageAttributes)
    }

    @Test
    fun `return a 404 when prison not in filter`() {
      val requestBody = asJsonString(cancelVisitRequest)

      postToApiWithCN(path, requestBody, limitedPrisonsCn)
        .andExpect(status().isNotFound)
    }

    @Test
    fun `return a 404 when no prisons in filter`() {
      val requestBody = asJsonString(cancelVisitRequest)

      postToApiWithCN(path, requestBody, noPrisonsCn)
        .andExpect(status().isNotFound)
    }
  }
}
