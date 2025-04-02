package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.assertions.json.shouldContainJsonKeyValue
import io.kotest.matchers.shouldBe
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpdateVisitRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.VisitContact
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.VisitNotes
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.VisitRestriction
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.VisitType
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Visitor
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.VisitorSupport
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.countAllMessagesOnQueue
import java.time.LocalDateTime

class VisitsIntegrationTest : IntegrationTestBase() {
  @Autowired
  protected lateinit var hmppsQueueService: HmppsQueueService

  internal val testQueue by lazy { hmppsQueueService.findByQueueId("visits") ?: throw RuntimeException("Queue with name visits doesn't exist") }
  internal val testSqsClient by lazy { testQueue.sqsClient }
  internal val testQueueUrl by lazy { testQueue.queueUrl }

  fun getNumberOfMessagesCurrentlyOnQueue(): Int = testSqsClient.countAllMessagesOnQueue(testQueueUrl).get()

  fun checkQueueIsEmpty() {
    getNumberOfMessagesCurrentlyOnQueue().shouldBe(0)
  }

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
    private val path = "/v1/visit"
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
        visitors = setOf(Visitor(nomisPersonId = 654321L, visitContact = true)),
        visitorSupport = VisitorSupport(description = "Visually impaired assistance"),
      )

    private fun getInvalidCreateVisitRequest(
      noPrisonerId: Boolean = false,
      noPrisonId: Boolean = false,
      noClientVisitReference: Boolean = false,
      noVisitRoom: Boolean = false,
      noNomisPersonId: Boolean = false,
      invalidNomisPersonId: Boolean = false,
      noVisitNoteType: Boolean = false,
      noVisitContactName: Boolean = false,
      noVisitorSupportDescription: Boolean = false,
    ) = CreateVisitRequest(
      prisonerId = if (noPrisonerId) "" else prisonerId,
      prisonId = if (noPrisonId) "" else "MDI",
      clientVisitReference = if (noClientVisitReference) "" else "123456",
      visitRoom = if (noVisitRoom) "" else "A1",
      visitType = VisitType.SOCIAL,
      visitRestriction = VisitRestriction.OPEN,
      startTimestamp = LocalDateTime.parse(timestamp),
      endTimestamp = LocalDateTime.parse(timestamp),
      visitNotes = listOf(VisitNotes(type = if (noVisitNoteType) "" else "VISITOR_CONCERN", text = "Visitor is concerned their mother in law is coming!")),
      visitContact = VisitContact(name = if (noVisitContactName) "" else "John Smith", telephone = "0987654321", email = "john.smith@example.com"),
      createDateTime = LocalDateTime.parse(timestamp),
      visitors =
        setOf(
          Visitor(
            nomisPersonId =
              if (noNomisPersonId) {
                0L
              } else if (invalidNomisPersonId) {
                123456L
              } else {
                654321L
              },
            visitContact = true,
          ),
        ),
      visitorSupport = VisitorSupport(description = if (noVisitorSupportDescription) "" else "Visually impaired assistance"),
    )

    @Test
    fun `post the visit, get back a message response and find a message on the queue`() {
      val createVisitRequest = getCreateVisitRequest(prisonerId)
      val requestBody = asJsonString(createVisitRequest)

      postToApi(path, requestBody)
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

      await untilCallTo { getNumberOfMessagesCurrentlyOnQueue() } matches { it == 1 }

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
    fun `post a visit with no prison id, should get 400 with no message on the queue`() {
      val createVisitRequest = getInvalidCreateVisitRequest(noPrisonId = true)
      val requestBody = asJsonString(createVisitRequest)

      postToApi(path, requestBody)
        .andExpect(status().isBadRequest)

      checkQueueIsEmpty()
    }

    @Test
    fun `post a visit with no prisoner id, should get 400 with no message on the queue`() {
      val createVisitRequest = getInvalidCreateVisitRequest(noPrisonerId = true)
      val requestBody = asJsonString(createVisitRequest)

      postToApi(path, requestBody)
        .andExpect(status().isBadRequest)

      checkQueueIsEmpty()
    }

    @Test
    fun `post a visit with no client visit reference, should get 400 with no message on the queue`() {
      val createVisitRequest = getInvalidCreateVisitRequest(noClientVisitReference = true)
      val requestBody = asJsonString(createVisitRequest)

      postToApi(path, requestBody)
        .andExpect(status().isBadRequest)

      checkQueueIsEmpty()
    }

    @Test
    fun `post a visit with no visit room, should get 400 with no message on the queue`() {
      val createVisitRequest = getInvalidCreateVisitRequest(noVisitRoom = true)
      val requestBody = asJsonString(createVisitRequest)

      postToApi(path, requestBody)
        .andExpect(status().isBadRequest)

      checkQueueIsEmpty()
    }

    @Test
    fun `post a visit with no nomisPersonId for the visitor, should get 400 with no message on the queue`() {
      val createVisitRequest = getInvalidCreateVisitRequest(noNomisPersonId = true)
      val requestBody = asJsonString(createVisitRequest)

      postToApi(path, requestBody)
        .andExpect(status().isBadRequest)

      checkQueueIsEmpty()
    }

    @Test
    fun `post a visit with no a nomisPersonId not part of the contacts for the visitor, should get 404 with no message on the queue`() {
      val createVisitRequest = getInvalidCreateVisitRequest(invalidNomisPersonId = true)
      val requestBody = asJsonString(createVisitRequest)

      postToApi(path, requestBody)
        .andExpect(status().isNotFound)

      checkQueueIsEmpty()
    }

    @Test
    fun `post a visit with no visit note type for the visit note, should get 400 with no message on the queue`() {
      val createVisitRequest = getInvalidCreateVisitRequest(noVisitNoteType = true)
      val requestBody = asJsonString(createVisitRequest)

      postToApi(path, requestBody)
        .andExpect(status().isBadRequest)

      checkQueueIsEmpty()
    }

    @Test
    fun `post a visit with no visit contact name for the visit contact, should get 400 with no message on the queue`() {
      val createVisitRequest = getInvalidCreateVisitRequest(noVisitContactName = true)
      val requestBody = asJsonString(createVisitRequest)

      postToApi(path, requestBody)
        .andExpect(status().isBadRequest)

      checkQueueIsEmpty()
    }

    @Test
    fun `post a visit with no visitor support description for the visit support, should get 400 with no message on the queue`() {
      val createVisitRequest = getInvalidCreateVisitRequest(noVisitorSupportDescription = true)
      val requestBody = asJsonString(createVisitRequest)

      postToApi(path, requestBody)
        .andExpect(status().isBadRequest)

      checkQueueIsEmpty()
    }

    @Test
    fun `return a 400 when prisoner ID not valid`() {
      val createVisitRequest = getCreateVisitRequest("INVALID_PRISON_ID")
      val requestBody = asJsonString(createVisitRequest)

      postToApiWithCN(path, requestBody, limitedPrisonsCn)
        .andExpect(status().isBadRequest)

      checkQueueIsEmpty()
    }

    @Test
    fun `return a 404 when prison not in filter`() {
      val createVisitRequest = getCreateVisitRequest(prisonerId)
      val requestBody = asJsonString(createVisitRequest)

      postToApiWithCN(path, requestBody, limitedPrisonsCn)
        .andExpect(status().isNotFound)

      checkQueueIsEmpty()
    }

    @Test
    fun `return a 404 when no prisons in filter`() {
      val createVisitRequest = getCreateVisitRequest(prisonerId)
      val requestBody = asJsonString(createVisitRequest)

      postToApiWithCN(path, requestBody, noPrisonsCn)
        .andExpect(status().isNotFound)

      checkQueueIsEmpty()
    }
  }

  @DisplayName("PUT /v1/visit/{visitReference}")
  @Nested
  inner class PutVisit {
    private val visitReference = "123456"
    private val path = "/v1/visit/$visitReference"
    private val timestamp = "2020-12-04T10:42:43"

    private val updateVisitRequest =
      UpdateVisitRequest(
        visitRoom = "A1",
        visitType = VisitType.SOCIAL,
        visitRestriction = VisitRestriction.OPEN,
        startTimestamp = LocalDateTime.parse(timestamp),
        endTimestamp = LocalDateTime.parse(timestamp),
        visitNotes = listOf(VisitNotes(type = "VISITOR_CONCERN", text = "Visitor is concerned their mother in law is coming!")),
        visitContact = VisitContact(name = "John Smith", telephone = "0987654321", email = "john.smith@example.com"),
        visitors = setOf(Visitor(nomisPersonId = 654321L, visitContact = true)),
        visitorSupport = VisitorSupport(description = "Visually impaired assistance"),
      )

    private fun getInvalidUpdateVisitRequest(
      noVisitRoom: Boolean = false,
      noNomisPersonId: Boolean = false,
      invalidNomisPersonId: Boolean = false,
      noVisitNoteType: Boolean = false,
      noVisitContactName: Boolean = false,
      noVisitorSupportDescription: Boolean = false,
    ) = UpdateVisitRequest(
      visitRoom = if (noVisitRoom) "" else "A1",
      visitType = VisitType.SOCIAL,
      visitRestriction = VisitRestriction.OPEN,
      startTimestamp = LocalDateTime.parse(timestamp),
      endTimestamp = LocalDateTime.parse(timestamp),
      visitNotes = listOf(VisitNotes(type = if (noVisitNoteType) "" else "VISITOR_CONCERN", text = "Visitor is concerned their mother in law is coming!")),
      visitContact = VisitContact(name = if (noVisitContactName) "" else "John Smith", telephone = "0987654321", email = "john.smith@example.com"),
      visitors =
        setOf(
          Visitor(
            nomisPersonId =
              if (noNomisPersonId) {
                0L
              } else if (invalidNomisPersonId) {
                123456L
              } else {
                654321L
              },
            visitContact = true,
          ),
        ),
      visitorSupport = VisitorSupport(description = if (noVisitorSupportDescription) "" else "Visually impaired assistance"),
    )

    @Test
    fun `put the visit update, get back a message response and find a message on the queue`() {
      val requestBody = asJsonString(updateVisitRequest)

      putApi(path, requestBody)
        .andExpect(status().isOk)
        .andExpect(
          content().json(
            """
            {
              "data": {
                  "message": "Visit update written to queue"
              }
            }
            """,
          ),
        )

      await untilCallTo { getNumberOfMessagesCurrentlyOnQueue() } matches { it == 1 }

      val queueMessages = getQueueMessages()
      queueMessages.size.shouldBe(1)

      val messageJson = queueMessages[0].body()
      val expectedMessage = updateVisitRequest.toHmppsMessage(defaultCn, visitReference)
      messageJson.shouldContainJsonKeyValue("$.eventType", expectedMessage.eventType.eventTypeCode)
      messageJson.shouldContainJsonKeyValue("$.who", defaultCn)
      val objectMapper = jacksonObjectMapper()
      val messageAttributes = objectMapper.readTree(messageJson).at("/messageAttributes")
      val expectedMessageAttributes = objectMapper.readTree(objectMapper.writeValueAsString(expectedMessage.messageAttributes))
      messageAttributes.shouldBe(expectedMessageAttributes)
    }

    @Test
    fun `put the visit update with no visit room, should get 400 with no message on the queue`() {
      val updateVisitRequest = getInvalidUpdateVisitRequest(noVisitRoom = true)
      val requestBody = asJsonString(updateVisitRequest)

      putApi(path, requestBody)
        .andExpect(status().isBadRequest)

      checkQueueIsEmpty()
    }

    @Test
    fun `put a visit update with no nomisPersonId for the visitor, should get 400 with no message on the queue`() {
      val updateVisitRequest = getInvalidUpdateVisitRequest(noNomisPersonId = true)
      val requestBody = asJsonString(updateVisitRequest)

      putApi(path, requestBody)
        .andExpect(status().isBadRequest)

      checkQueueIsEmpty()
    }

    @Test
    fun `put a visit update with no a nomisPersonId not part of the contacts for the visitor, should get 404 with no message on the queue`() {
      val updateVisitRequest = getInvalidUpdateVisitRequest(invalidNomisPersonId = true)
      val requestBody = asJsonString(updateVisitRequest)

      putApi(path, requestBody)
        .andExpect(status().isNotFound)

      checkQueueIsEmpty()
    }

    @Test
    fun `put a visit update with no visit note type for the visit note, should get 400 with no message on the queue`() {
      val updateVisitRequest = getInvalidUpdateVisitRequest(noVisitNoteType = true)
      val requestBody = asJsonString(updateVisitRequest)

      putApi(path, requestBody)
        .andExpect(status().isBadRequest)

      checkQueueIsEmpty()
    }

    @Test
    fun `put a visit update with no visit contact name for the visit contact, should get 400 with no message on the queue`() {
      val updateVisitRequest = getInvalidUpdateVisitRequest(noVisitContactName = true)
      val requestBody = asJsonString(updateVisitRequest)

      putApi(path, requestBody)
        .andExpect(status().isBadRequest)

      checkQueueIsEmpty()
    }

    @Test
    fun `put a visit update with no visitor support description for the visit support, should get 400 with no message on the queue`() {
      val updateVisitRequest = getInvalidUpdateVisitRequest(noVisitorSupportDescription = true)
      val requestBody = asJsonString(updateVisitRequest)

      putApi(path, requestBody)
        .andExpect(status().isBadRequest)

      checkQueueIsEmpty()
    }

    @Test
    fun `return a 404 when prison not in filter`() {
      val requestBody = asJsonString(updateVisitRequest)

      putApiWithCN(path, requestBody, limitedPrisonsCn)
        .andExpect(status().isNotFound)

      checkQueueIsEmpty()
    }

    @Test
    fun `return a 404 when no prisons in filter`() {
      val requestBody = asJsonString(updateVisitRequest)

      putApiWithCN(path, requestBody, noPrisonsCn)
        .andExpect(status().isNotFound)

      checkQueueIsEmpty()
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

    private fun getInvalidCancelVisitRequest(noActionedBy: Boolean = false) =
      CancelVisitRequest(
        cancelOutcome =
          CancelOutcome(
            outcomeStatus = OutcomeStatus.VISIT_ORDER_CANCELLED,
            text = "visit order cancelled",
          ),
        actionedBy = if (noActionedBy) "" else "test-consumer",
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
    fun `post a visit cancellation with no actioned by, should get 400 with no message on the queue`() {
      val cancelVisitRequest = getInvalidCancelVisitRequest(noActionedBy = true)
      val requestBody = asJsonString(cancelVisitRequest)

      postToApi(path, requestBody)
        .andExpect(status().isBadRequest)

      checkQueueIsEmpty()
    }

    @Test
    fun `return a 404 when prison not in filter`() {
      val requestBody = asJsonString(cancelVisitRequest)

      postToApiWithCN(path, requestBody, limitedPrisonsCn)
        .andExpect(status().isNotFound)

      checkQueueIsEmpty()
    }

    @Test
    fun `return a 404 when no prisons in filter`() {
      val requestBody = asJsonString(cancelVisitRequest)

      postToApiWithCN(path, requestBody, noPrisonsCn)
        .andExpect(status().isNotFound)

      checkQueueIsEmpty()
    }
  }

  @DisplayName("GET /id/by-client-ref/{clientReference}")
  @Nested
  inner class GetClientRefByVisitRef {
    private val clientReference = "AABDC234"

    @Test
    fun `gets the visit reference`() {
      callApi("/v1/visit/id/by-client-ref/$clientReference")
        .andExpect(status().isOk)
        .andExpect(
          content().json(
            """
              {
                "data": {
                  "visitReferences": ["abc-123-xyz"]
                }
              }
              """,
          ),
        )
    }

    @Test
    fun `return a 404 when prison not in filter`() {
      callApiWithCN("/v1/visit/id/by-client-ref/$clientReference", limitedPrisonsCn)
        .andExpect(status().isNotFound)
    }

    @Test
    fun `return a 404 when no prisons in filter`() {
      callApiWithCN("/v1/visit/id/by-client-ref/$clientReference", noPrisonsCn)
        .andExpect(status().isNotFound)
    }
  }
}
