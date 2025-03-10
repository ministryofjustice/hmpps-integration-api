package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import io.kotest.assertions.json.shouldContainJsonKeyValue
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Visit
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.VisitContact
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.VisitorSupport
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetFutureVisitsService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

@WebMvcTest(controllers = [FutureVisitsController::class])
@ActiveProfiles("test")
class FutureVisitsControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @MockitoBean val getPersonService: GetPersonService,
  @MockitoBean val getFutureVisitsService: GetFutureVisitsService,
  @MockitoBean val auditService: AuditService,
) : DescribeSpec({
    val mockMvc = IntegrationAPIMockMvc(springMockMvc)
    val hmppsId = "G6980GG"
    val path = "/v1/persons/$hmppsId/visit/future"
    val futureVisits =
      listOf(
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
          visitorSupport = VisitorSupport(description = "Description"),
          applicationReference = "dfs-wjs-abc",
          reference = "dfs-wjs-abc",
          sessionTemplateReference = "dfs-wjs-xyz",
        ),
      )

    beforeTest {
      Mockito.reset(getFutureVisitsService)
      Mockito.reset(auditService)

      whenever(getFutureVisitsService.execute(hmppsId, filters = null)).thenReturn(Response(data = futureVisits))
    }

    it("Returns a 200 response with data") {
      val result = mockMvc.performAuthorised(path)
      result.response.status.shouldBe(HttpStatus.OK.value())
      result.response.contentAsString.shouldContainJsonKeyValue("$.data.[0].applicationReference", futureVisits[0].applicationReference)
    }

    it("Calls audit service") {
      mockMvc.performAuthorised(path)

      verify(auditService, times(1)).createEvent("GET_FUTURE_VISITS", mapOf("hmppsId" to hmppsId))
    }

    it("Handles 400 bad request") {
      whenever(getFutureVisitsService.execute(hmppsId, filters = null)).thenReturn(Response(data = null, errors = listOf(UpstreamApiError(UpstreamApi.MANAGE_PRISON_VISITS, UpstreamApiError.Type.BAD_REQUEST))))

      val result = mockMvc.performAuthorised(path)
      result.response.status.shouldBe(HttpStatus.BAD_REQUEST.value())
    }

    it("Handles 404 entity not found") {
      whenever(getFutureVisitsService.execute(hmppsId, filters = null)).thenReturn(Response(data = null, errors = listOf(UpstreamApiError(UpstreamApi.MANAGE_PRISON_VISITS, UpstreamApiError.Type.ENTITY_NOT_FOUND))))

      val result = mockMvc.performAuthorised(path)
      result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
    }

    it("Handles 500 internal server error") {
      whenever(getFutureVisitsService.execute(hmppsId, filters = null)).thenThrow(IllegalStateException("Internal Server Error"))

      val result = mockMvc.performAuthorised(path)
      result.response.status.shouldBe(HttpStatus.INTERNAL_SERVER_ERROR.value())
    }
  })
