package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Visit
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.VisitContact
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.VisitorSupport
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetVisitInformationByReferenceService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.VisitQueueService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

@WebMvcTest(controllers = [VisitsController::class])
@ActiveProfiles("test")
class VisitsControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @MockitoBean val auditService: AuditService,
  @MockitoBean val getVisitInformationByReferenceService: GetVisitInformationByReferenceService,
  @MockitoBean val visitQueueService: VisitQueueService,
) : DescribeSpec(
    {
      val visitReference = "1234567"
      val path = "/v1/visit/$visitReference"
      val mockMvc = IntegrationAPIMockMvc(springMockMvc)
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

      it("calls the visit information service and successfully retrieves the visit information") {
        whenever(getVisitInformationByReferenceService.execute(visitReference)).thenReturn(Response(data = visitResponse))

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
        verify(getVisitInformationByReferenceService, VerificationModeFactory.times(1)).execute(visitReference)
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
    },
  )
