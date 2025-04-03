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
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.MessageFailedException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PersonalRelationshipsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CancelOutcome
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CancelVisitRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CreateVisitRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsMessage
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsMessageResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.NomisNumber
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.OutcomeStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpdateVisitRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UserType
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Visit
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.VisitContact
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.VisitExternalSystemDetails
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.VisitNotes
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.VisitRestriction
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.VisitType
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Visitor
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.VisitorSupport
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Visitors
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships.PRPaginatedPrisonerContacts
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships.PRPrisonerContact
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships.Pageable
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships.Sort
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.hmpps.sqs.HmppsQueue
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import java.time.LocalDateTime

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [VisitQueueService::class],
)
internal class VisitQueueServiceTest(
  private val visitQueueService: VisitQueueService,
  @MockitoBean val getPersonService: GetPersonService,
  @MockitoBean val hmppsQueueService: HmppsQueueService,
  @MockitoBean val objectMapper: ObjectMapper,
  @MockitoBean val getVisitInformationByReferenceService: GetVisitInformationByReferenceService,
  @MockitoBean val personalRelationshipsGateway: PersonalRelationshipsGateway,
  @MockitoBean val consumerPrisonAccessService: ConsumerPrisonAccessService,
) : DescribeSpec({
    val mockSqsClient = mock<SqsAsyncClient>()
    val visitQueue =
      mock<HmppsQueue> {
        on { sqsClient } doReturn mockSqsClient
        on { queueUrl } doReturn "https://test-queue-url"
      }

    val hmppsId = "A1234AB"
    val prisonId = "MDI"
    val filters = ConsumerFilters(prisons = listOf(prisonId))
    val who = "client-name"
    val visitReference = "ABC-123-DEF-456"
    val visitorContactId = 3L
    val visitor = Visitors(contactId = visitorContactId, visitContact = true)
    val visitResponse =
      Visit(
        prisonerId = hmppsId,
        prisonId = prisonId,
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
        visitors = listOf(visitor),
        visitNotes = emptyList(),
        visitContact = VisitContact(name = "Name", telephone = "Telephone", email = "Email"),
        applicationReference = "dfs-wjs-abc",
        reference = "dfs-wjs-abc",
        sessionTemplateReference = "dfs-wjs-xyz",
        visitorSupport = VisitorSupport(description = "Description"),
        visitExternalSystemDetails =
          VisitExternalSystemDetails(
            clientName = "client_name",
            clientVisitReference = "12345",
          ),
      )

    fun getContactsResponse(visitorContactId: Long): PRPaginatedPrisonerContacts =
      PRPaginatedPrisonerContacts(
        contacts =
          listOf(
            PRPrisonerContact(
              prisonerContactId = 123456,
              contactId = visitorContactId,
              prisonerNumber = "A1234BC",
              lastName = "Doe",
              firstName = "John",
              middleNames = "William",
              dateOfBirth = "1980-01-01",
              relationshipTypeCode = "S",
              relationshipTypeDescription = "Friend",
              relationshipToPrisonerCode = "FRI",
              relationshipToPrisonerDescription = "Friend",
              flat = "Flat 1",
              property = "123",
              street = "Baker Street",
              area = "Marylebone",
              cityCode = "25343",
              cityDescription = "Sheffield",
              countyCode = "S.YORKSHIRE",
              countyDescription = "South Yorkshire",
              postCode = "NW1 6XE",
              countryCode = "ENG",
              countryDescription = "England",
              primaryAddress = true,
              mailAddress = true,
              phoneType = "MOB",
              phoneTypeDescription = "Mobile",
              phoneNumber = "+1234567890",
              extNumber = "123",
              approvedVisitor = true,
              nextOfKin = false,
              emergencyContact = true,
              isRelationshipActive = true,
              currentTerm = true,
              comments = "Close family friend",
            ),
          ),
        pageable =
          Pageable(
            offset = 0,
            sort =
              Sort(
                empty = false,
                sorted = false,
                unsorted = true,
              ),
            pageSize = 10,
            paged = true,
            pageNumber = 1,
            unpaged = false,
          ),
        totalElements = 1,
        totalPages = 1,
        first = true,
        last = true,
        size = 10,
        number = 1,
        sort =
          Sort(
            empty = false,
            sorted = false,
            unsorted = true,
          ),
        numberOfElements = 1,
        empty = false,
      )

    beforeTest {
      reset(mockSqsClient, objectMapper)

      whenever(hmppsQueueService.findByQueueId("visits")).thenReturn(visitQueue)
      whenever(getPersonService.getNomisNumberWithPrisonFilter(hmppsId = eq(hmppsId), filters = any<ConsumerFilters>())).thenReturn(Response(NomisNumber(hmppsId)))
      whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<HmppsMessage>(prisonId, filters)).thenReturn(Response(data = null))
      whenever(getVisitInformationByReferenceService.execute(eq(visitReference), any<ConsumerFilters>())).thenReturn(Response(data = visitResponse))
      whenever(personalRelationshipsGateway.getContacts(hmppsId, page = 1, size = 10)).thenReturn(
        Response(
          data = getContactsResponse(visitorContactId),
        ),
      )
    }

    describe("create visit message") {
      val createVisitRequest =
        CreateVisitRequest(
          prisonerId = hmppsId,
          prisonId = prisonId,
          clientVisitReference = "123456",
          visitRoom = "A1",
          visitType = VisitType.SOCIAL,
          visitRestriction = VisitRestriction.OPEN,
          startTimestamp = LocalDateTime.parse("2020-12-04T10:42:43"),
          endTimestamp = LocalDateTime.parse("2020-12-04T10:42:43"),
          visitNotes = listOf(VisitNotes(type = "VISITOR_CONCERN", text = "Visitor is concerned their mother in law is coming!")),
          visitContact = VisitContact(name = "John Smith", telephone = "0987654321", email = "john.smith@example.com"),
          createDateTime = LocalDateTime.parse("2020-12-04T10:42:43"),
          visitors = setOf(Visitor(nomisPersonId = visitorContactId, visitContact = true)),
          visitorSupport = VisitorSupport(description = "Visually impaired assistance"),
        )

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

        exception.message.shouldBe("Could not send Visit create to queue")
      }

      it("return error if getPersonService returns an error") {
        val errors = listOf(UpstreamApiError(UpstreamApi.NOMIS, UpstreamApiError.Type.INTERNAL_SERVER_ERROR, description = "getPersonService returns an error"))
        whenever(getPersonService.getNomisNumberWithPrisonFilter(hmppsId = hmppsId, filters = filters)).thenReturn(Response(data = null, errors))

        val response = visitQueueService.sendCreateVisit(createVisitRequest, who, filters)
        response.data.shouldBeNull()
        response.errors.shouldBe(errors)
      }

      it("return error if consumerPrisonAccessService returns an error") {
        val incorrectFilters = ConsumerFilters(prisons = listOf("XYZ"))
        val errors = listOf(UpstreamApiError(UpstreamApi.NOMIS, UpstreamApiError.Type.ENTITY_NOT_FOUND, description = "consumerPrisonAccessService returns an error"))
        whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<HmppsMessage>(createVisitRequest.prisonId, incorrectFilters)).thenReturn(Response(data = null, errors))

        val response = visitQueueService.sendCreateVisit(createVisitRequest, who, incorrectFilters)
        response.data.shouldBeNull()
        response.errors.shouldBe(errors)
      }

      it("return error if personalRelationshipsGateway returns an error") {
        val errors = listOf(UpstreamApiError(UpstreamApi.PERSONAL_RELATIONSHIPS, UpstreamApiError.Type.INTERNAL_SERVER_ERROR, description = "personalRelationshipsGateway returns an error"))
        whenever(personalRelationshipsGateway.getContacts(prisonerId = hmppsId, page = 1, size = 10)).thenReturn(Response(data = null, errors))

        val response = visitQueueService.sendCreateVisit(createVisitRequest, who, filters)
        response.data.shouldBeNull()
        response.errors.shouldBe(errors)
      }

      it("return error if the visitor is not a contact of the prisoner") {
        whenever(personalRelationshipsGateway.getContacts(prisonerId = hmppsId, page = 1, size = 10)).thenReturn(
          Response(
            data = getContactsResponse(visitorContactId + 1), // Add 1 so it is different from expected
          ),
        )

        val response = visitQueueService.sendCreateVisit(createVisitRequest, who, filters)
        response.data.shouldBeNull()
        response.errors.shouldBe(listOf(UpstreamApiError(UpstreamApi.PERSONAL_RELATIONSHIPS, UpstreamApiError.Type.ENTITY_NOT_FOUND, "No contact found with an ID of $visitorContactId")))
      }
    }

    describe("update visit request") {
      val updateVisitRequest =
        UpdateVisitRequest(
          visitRoom = "A1",
          visitType = VisitType.SOCIAL,
          visitRestriction = VisitRestriction.OPEN,
          startTimestamp = LocalDateTime.parse("2020-12-04T10:42:43"),
          endTimestamp = LocalDateTime.parse("2020-12-04T10:42:43"),
          visitNotes = listOf(VisitNotes(type = "VISITOR_CONCERN", text = "Visitor is concerned their mother in law is coming!")),
          visitContact = VisitContact(name = "John Smith", telephone = "0987654321", email = "john.smith@example.com"),
          visitors = setOf(Visitor(nomisPersonId = visitorContactId, visitContact = true)),
          visitorSupport = VisitorSupport(description = "Visually impaired assistance"),
        )

      it("successfully adds to message queue") {
        val messageBody = """{"messageId":"1","eventType":"VisitUpdated","messageAttributes":{}}"""

        whenever(objectMapper.writeValueAsString(any<HmppsMessage>())).thenReturn(messageBody)

        val response = visitQueueService.sendUpdateVisit(visitReference, updateVisitRequest, who, filters)

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
            visitQueueService.sendUpdateVisit(visitReference, updateVisitRequest, who, filters)
          }

        exception.message.shouldBe("Could not send Visit update to queue")
      }

      it("return error if getVisitInformationByReferenceService returns an error") {
        val errors = listOf(UpstreamApiError(UpstreamApi.MANAGE_PRISON_VISITS, UpstreamApiError.Type.INTERNAL_SERVER_ERROR, description = "getVisitInformationByReferenceService returns an error"))
        whenever(getVisitInformationByReferenceService.execute(visitReference, filters = filters)).thenReturn(Response(data = null, errors))

        val response = visitQueueService.sendUpdateVisit(visitReference, updateVisitRequest, who, filters)
        response.data.shouldBeNull()
        response.errors.shouldBe(errors)
      }

      it("return error if personalRelationshipsGateway returns an error") {
        val errors = listOf(UpstreamApiError(UpstreamApi.PERSONAL_RELATIONSHIPS, UpstreamApiError.Type.INTERNAL_SERVER_ERROR, description = "personalRelationshipsGateway returns an error"))
        whenever(personalRelationshipsGateway.getContacts(prisonerId = hmppsId, page = 1, size = 10)).thenReturn(Response(data = null, errors))

        val response = visitQueueService.sendUpdateVisit(visitReference, updateVisitRequest, who, filters)
        response.data.shouldBeNull()
        response.errors.shouldBe(errors)
      }

      it("return error if the visitor is not a contact of the prisoner") {
        whenever(personalRelationshipsGateway.getContacts(prisonerId = hmppsId, page = 1, size = 10)).thenReturn(
          Response(
            data = getContactsResponse(visitorContactId + 1), // Add 1 so it is different from expected
          ),
        )

        val response = visitQueueService.sendUpdateVisit(visitReference, updateVisitRequest, who, filters)
        response.data.shouldBeNull()
        response.errors.shouldBe(listOf(UpstreamApiError(UpstreamApi.PERSONAL_RELATIONSHIPS, UpstreamApiError.Type.ENTITY_NOT_FOUND, "No contact found with an ID of $visitorContactId")))
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
          userType = UserType.PRISONER,
          actionedBy = "someUser",
        )

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
