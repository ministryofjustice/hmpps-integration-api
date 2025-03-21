package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.CrnSupplier
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.MessageFailedException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.GetCaseAccess
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CancelOutcome
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CancelVisitRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CreateVisitRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsMessageResponse
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetVisitInformationByReferenceService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.VisitQueueService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService
import java.time.LocalDateTime

@WebMvcTest(controllers = [VisitsController::class])
@ActiveProfiles("test")
class VisitsControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @MockitoBean val auditService: AuditService,
  @MockitoBean val getVisitInformationByReferenceService: GetVisitInformationByReferenceService,
  @MockitoBean val visitQueueService: VisitQueueService,
  @MockitoBean val crnSupplier: CrnSupplier,
  @MockitoBean val getCaseAccess: GetCaseAccess,
) : DescribeSpec(
    {
      val mockMvc = IntegrationAPIMockMvc(springMockMvc)

      beforeTest {
        Mockito.reset(auditService)
      }

      describe("/v1/visit/{visitReference}") {
        val visitReference = "1234567"
        val path = "/v1/visit/$visitReference"
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
          Mockito.reset(getVisitInformationByReferenceService)

          whenever(getVisitInformationByReferenceService.execute(visitReference)).thenReturn(Response(data = visitResponse))
        }

        it("logs audit") {
          mockMvc.performAuthorised(path)

          verify(
            auditService,
            times(1),
          ).createEvent("GET_VISIT_INFORMATION_BY_REFERENCE", mapOf("visitReference" to visitReference))
        }

        it("calls the visit information service and successfully retrieves the visit information") {
          val result = mockMvc.performAuthorised(path)
          result.response.status.shouldBe(HttpStatus.OK.value())
          result.response.contentAsString shouldBe (
            """
            {
              "data": {
                  "applicationReference":"dfs-wjs-abc",
                  "reference":"dfs-wjs-abc",
                  "prisonerId": "PrisonerId",
                  "prisonId": "MDI",
                  "prisonName": "Some Prison",
                  "sessionTemplateReference":"dfs-wjs-xyz",
                  "visitRoom": "Room",
                  "visitType": "Type",
                  "visitStatus": "Status",
                  "outcomeStatus": "Outcome",
                  "visitRestriction": "Restriction",
                  "startTimestamp": "Start",
                  "endTimestamp": "End",
                  "visitNotes": [],
                  "visitContact": {"name": "Name", "telephone": "Telephone", "email": "Email"},
                  "visitors": [],
                  "visitorSupport": {"description": "Description"},
                  "createdTimestamp": "Created",
                  "modifiedTimestamp": "Modified",
                  "firstBookedDateTime": "First"
              }
            }
          """.removeWhitespaceAndNewlines()
          )
          verify(getVisitInformationByReferenceService, times(1)).execute(visitReference)
        }

        it("gets a 404 when visit not found by reference") {
          whenever(getVisitInformationByReferenceService.execute(visitReference)).thenReturn(Response(data = null, errors = listOf(UpstreamApiError(causedBy = UpstreamApi.MANAGE_PRISON_VISITS, type = UpstreamApiError.Type.ENTITY_NOT_FOUND))))

          val result = mockMvc.performAuthorised(path)
          result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
        }

        it("gets a 500 when visit service responds") {
          whenever(getVisitInformationByReferenceService.execute(visitReference)).thenReturn(Response(data = null, errors = listOf(UpstreamApiError(causedBy = UpstreamApi.MANAGE_PRISON_VISITS, type = UpstreamApiError.Type.INTERNAL_SERVER_ERROR))))

          val result = mockMvc.performAuthorised(path)
          result.response.status.shouldBe(HttpStatus.INTERNAL_SERVER_ERROR.value())
        }
      }

      describe("/v1/visit") {
        val path = "/v1/visit"
        val filters = null
        val message = "Visit Message"
        val postResponse = HmppsMessageResponse(message = message)
        val clientName = "automated-test-client"
        val timestamp = "2020-12-04T10:42:43"
        val createVisitRequest =
          CreateVisitRequest(
            prisonerId = "A1234AB",
            prisonId = "MDI",
            clientVisitReference = "123456",
            visitRoom = "A1",
            visitType = VisitType.SOCIAL,
            visitStatus = VisitStatus.BOOKED,
            visitRestriction = VisitRestriction.OPEN,
            startTimestamp = LocalDateTime.parse(timestamp),
            endTimestamp = LocalDateTime.parse(timestamp),
            createDateTime = LocalDateTime.parse(timestamp),
            visitors = setOf(Visitor(nomisPersonId = 3L, visitContact = true)),
            actionedBy = clientName,
          )

        beforeTest {
          Mockito.reset(visitQueueService)

          whenever(visitQueueService.sendCreateVisit(createVisitRequest, clientName, filters)).thenReturn(Response(data = postResponse))
        }

        it("logs audit") {
          mockMvc.performAuthorisedPost(path, createVisitRequest)

          verify(
            auditService,
            times(1),
          ).createEvent("POST_VISIT", mapOf("prisonerId" to createVisitRequest.prisonerId, "clientVisitReference" to createVisitRequest.clientVisitReference, "clientName" to clientName))
        }

        it("Calls the visit queue service and gets a response") {
          val result = mockMvc.performAuthorisedPost(path, createVisitRequest)
          result.response.status.shouldBe(HttpStatus.OK.value())
          result.response.contentAsString shouldBe (
            """
            {
              "data": {
                "message": "$message"
              }
            }
          """.removeWhitespaceAndNewlines()
          )
        }

        it("returns a 400 when upstream returns 400") {
          whenever(visitQueueService.sendCreateVisit(createVisitRequest, clientName, filters)).thenReturn(Response(data = null, errors = listOf(UpstreamApiError(causedBy = UpstreamApi.MANAGE_PRISON_VISITS, type = UpstreamApiError.Type.BAD_REQUEST))))

          val result = mockMvc.performAuthorisedPost(path, createVisitRequest)
          result.response.status.shouldBe(HttpStatus.BAD_REQUEST.value())
        }

        it("returns a 404 when upstream returns 404") {
          whenever(visitQueueService.sendCreateVisit(createVisitRequest, clientName, filters)).thenReturn(Response(data = null, errors = listOf(UpstreamApiError(causedBy = UpstreamApi.MANAGE_PRISON_VISITS, type = UpstreamApiError.Type.ENTITY_NOT_FOUND))))

          val result = mockMvc.performAuthorisedPost(path, createVisitRequest)
          result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
        }

        it("gets a 500 when visit queue service throws MessageFailedException") {
          whenever(visitQueueService.sendCreateVisit(createVisitRequest, clientName, filters)).thenThrow(MessageFailedException("Could not send Visit message to queue"))

          val result = mockMvc.performAuthorisedPost(path, createVisitRequest)
          result.response.status.shouldBe(HttpStatus.INTERNAL_SERVER_ERROR.value())
        }
      }

      describe("/v1/visit/{visitReference}/cancel") {
        val visitReference = "1234567"
        val path = "/v1/visit/$visitReference/cancel"
        val filters = null
        val message = "Visit Message"
        val postResponse = HmppsMessageResponse(message = message)
        val clientName = "automated-test-client"
        val cancelVisitRequest =
          CancelVisitRequest(
            cancelOutcome =
              CancelOutcome(
                outcomeStatus = OutcomeStatus.VISIT_ORDER_CANCELLED,
                text = "Visitor has informed us they cannot make the visit.",
              ),
            actionedBy = "someUser",
          )

        beforeTest {
          Mockito.reset(visitQueueService)

          whenever(visitQueueService.sendCancelVisit(visitReference, cancelVisitRequest, clientName, filters)).thenReturn(Response(data = postResponse))
        }

        it("logs audit") {
          mockMvc.performAuthorisedPost(path, cancelVisitRequest)

          verify(
            auditService,
            times(1),
          ).createEvent("POST_CANCEL_VISIT", mapOf("visitReference" to visitReference, "clientName" to clientName))
        }

        it("Calls the visit queue service and gets a response") {
          val result = mockMvc.performAuthorisedPost(path, cancelVisitRequest)
          result.response.status.shouldBe(HttpStatus.OK.value())
          result.response.contentAsString shouldBe (
            """
            {
              "data": {
                "message": "$message"
              }
            }
          """.removeWhitespaceAndNewlines()
          )
        }

        it("returns a 400 when upstream returns 400") {
          whenever(visitQueueService.sendCancelVisit(visitReference, cancelVisitRequest, clientName, filters)).thenReturn(Response(data = null, errors = listOf(UpstreamApiError(causedBy = UpstreamApi.MANAGE_PRISON_VISITS, type = UpstreamApiError.Type.BAD_REQUEST))))

          val result = mockMvc.performAuthorisedPost(path, cancelVisitRequest)
          result.response.status.shouldBe(HttpStatus.BAD_REQUEST.value())
        }

        it("returns a 404 when upstream returns 404") {
          whenever(visitQueueService.sendCancelVisit(visitReference, cancelVisitRequest, clientName, filters)).thenReturn(Response(data = null, errors = listOf(UpstreamApiError(causedBy = UpstreamApi.MANAGE_PRISON_VISITS, type = UpstreamApiError.Type.ENTITY_NOT_FOUND))))

          val result = mockMvc.performAuthorisedPost(path, cancelVisitRequest)
          result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
        }

        it("gets a 500 when visit queue service throws MessageFailedException") {
          whenever(visitQueueService.sendCancelVisit(visitReference, cancelVisitRequest, clientName, filters)).thenThrow(MessageFailedException("Could not send Visit message to queue"))

          val result = mockMvc.performAuthorisedPost(path, cancelVisitRequest)
          result.response.status.shouldBe(HttpStatus.INTERNAL_SERVER_ERROR.value())
        }
      }
    },
  )
