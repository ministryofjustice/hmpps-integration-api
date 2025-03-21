package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.MessageFailedException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CancelOutcome
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CancelVisitRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CreateVisitRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsMessage
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsMessageResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.NomisNumber
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.OutcomeStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Visit
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.VisitContact
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.VisitRestriction
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.VisitStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.VisitType
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Visitor
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.VisitorSupport
import uk.gov.justice.hmpps.sqs.HmppsQueue
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import java.time.LocalDateTime

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [VisitQueueService::class],
)
internal class VisitQueueServiceTest(
  @MockitoBean val getPersonService: GetPersonService,
  @MockitoBean val hmppsQueueService: HmppsQueueService,
  private val visitQueueService: VisitQueueService,
  @MockitoBean val objectMapper: ObjectMapper,
  @MockitoBean val getVisitInformationByReferenceService: GetVisitInformationByReferenceService,
) : DescribeSpec({

    val mockSqsClient = mock<SqsAsyncClient>()

    val visitQueue =
      mock<HmppsQueue> {
        on { sqsClient } doReturn mockSqsClient
        on { queueUrl } doReturn "https://test-queue-url"
      }

    val hmppsId = "A1234AB"
    val filters = null
    val visitReference = "ABC-123-DEF-456"
    val visitResponse =
      Visit(
        prisonerId = "PrisonerId",
        prisonId = "MDI",
        prisonName = "Some Prison",
        visitRoom = "Room",
        visitType = "Type",
        visitStatus = "Status",
        outcomeStatus = "Outcome",
        visitRestriction = "Restriction",
        startTimestamp = "Start",
        endTimestamp = "End",
        createdTimestamp = "Created",
        modifiedTimestamp = "Modified",
        firstBookedDateTime = "First",
        visitors = emptyList(),
        visitNotes = emptyList(),
        visitContact = VisitContact(name = "Name", telephone = "Telephone", email = "Email"),
        applicationReference = "dfs-wjs-abc",
        reference = "dfs-wjs-abc",
        sessionTemplateReference = "dfs-wjs-xyz",
        visitorSupport = VisitorSupport(description = "Description"),
      )

    beforeTest {
      reset(mockSqsClient, objectMapper)
      whenever(hmppsQueueService.findByQueueId("visits")).thenReturn(visitQueue)
      whenever(getPersonService.getNomisNumberWithPrisonFilter(hmppsId = hmppsId, filters = filters)).thenReturn(Response(NomisNumber(hmppsId)))
      whenever(getVisitInformationByReferenceService.execute(visitReference, filters)).thenReturn(Response(data = visitResponse))
    }

    describe("create visit message") {
      val createVisitRequest =
        CreateVisitRequest(
          prisonerId = "A1234AB",
          prisonId = "MDI",
          clientVisitReference = "123456",
          visitRoom = "A1",
          visitType = VisitType.SOCIAL,
          visitStatus = VisitStatus.BOOKED,
          visitRestriction = VisitRestriction.OPEN,
          startTimestamp = LocalDateTime.parse("2020-12-04T10:42:43"),
          endTimestamp = LocalDateTime.parse("2020-12-04T10:42:43"),
          createDateTime = LocalDateTime.parse("2020-12-04T10:42:43"),
          visitors = setOf(Visitor(nomisPersonId = 3L, visitContact = true)),
          actionedBy = "test-consumer",
        )
      val who = "client-name"

      it("successfully adds to message queue") {
        val messageBody = """{"messageId":"1","eventType":"VisitCreated","messageAttributes":{}}"""

        whenever(objectMapper.writeValueAsString(any<HmppsMessage>())).thenReturn(messageBody)

        val response = visitQueueService.sendCreateVisit(createVisitRequest, who, filters)

        verify(mockSqsClient).sendMessage(
          argThat<SendMessageRequest> { request: SendMessageRequest? ->
            request?.queueUrl() == "https://test-queue-url" &&
              request.messageBody() == messageBody
          },
        )

        response.data.shouldBeTypeOf<HmppsMessageResponse>()
      }

      it("should throw message failed exception if fails to write to the queue") {
        whenever(mockSqsClient.sendMessage(any<SendMessageRequest>()))
          .thenThrow(RuntimeException("Failed to send message to SQS"))

        val exception =
          shouldThrow<MessageFailedException> {
            visitQueueService.sendCreateVisit(createVisitRequest, who, filters)
          }

        exception.message.shouldBe("Could not send Visit message to queue")
      }

      it("return error if getPersonService returns an error") {
        val errors = listOf(UpstreamApiError(UpstreamApi.NOMIS, UpstreamApiError.Type.INTERNAL_SERVER_ERROR, description = "getPersonService returns an error"))
        whenever(getPersonService.getNomisNumberWithPrisonFilter(hmppsId = hmppsId, filters = filters)).thenReturn(Response(data = null, errors))

        val response = visitQueueService.sendCreateVisit(createVisitRequest, who, filters)
        response.data.shouldBeNull()
        response.errors.shouldBe(errors)
      }
    }

    describe("cancel visit request") {
      val cancelVisitRequest =
        CancelVisitRequest(
          cancelOutcome =
            CancelOutcome(
              outcomeStatus = OutcomeStatus.VISIT_ORDER_CANCELLED,
              text = "Visitor has informed us they cannot make the visit.",
            ),
          actionedBy = "someUser",
        )
      val who = "client-name"

      it("successfully adds to message queue") {
        val messageBody = """{"messageId":"1","eventType":"VisitCancelled","messageAttributes":{}}"""

        whenever(objectMapper.writeValueAsString(any<HmppsMessage>())).thenReturn(messageBody)

        val response = visitQueueService.sendCancelVisit(visitReference, cancelVisitRequest, who, filters)

        verify(mockSqsClient).sendMessage(
          argThat<SendMessageRequest> { request: SendMessageRequest? ->
            request?.queueUrl() == "https://test-queue-url" &&
              request.messageBody() == messageBody
          },
        )

        response.data.shouldBeTypeOf<HmppsMessageResponse>()
      }

      it("should throw message failed exception if fails to write to the queue") {
        whenever(mockSqsClient.sendMessage(any<SendMessageRequest>()))
          .thenThrow(RuntimeException("Failed to send message to SQS"))

        val exception =
          shouldThrow<MessageFailedException> {
            visitQueueService.sendCancelVisit(visitReference, cancelVisitRequest, who, filters)
          }

        exception.message.shouldBe("Could not send Visit cancellation to queue")
      }

      it("return error if getVisitInformationByReferenceService returns an error") {
        val errors = listOf(UpstreamApiError(UpstreamApi.MANAGE_PRISON_VISITS, UpstreamApiError.Type.INTERNAL_SERVER_ERROR, description = "getVisitInformationByReferenceService returns an error"))
        whenever(getVisitInformationByReferenceService.execute(visitReference, filters = filters)).thenReturn(Response(data = null, errors))

        val response = visitQueueService.sendCancelVisit(visitReference, cancelVisitRequest, who, filters)
        response.data.shouldBeNull()
        response.errors.shouldBe(errors)
      }
    }
  })
