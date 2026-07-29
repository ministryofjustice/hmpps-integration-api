package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1

import io.kotest.assertions.json.shouldContainJsonKeyValue
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.WebMvcTestConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CourtCasesSummary
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CourtOutcome
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.remandAndSentencing.CourtOutComeType
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.CourtCaseService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService
import java.time.LocalDate

@WebMvcTest(controllers = [CourtCaseController::class])
@Import(WebMvcTestConfiguration::class)
@ActiveProfiles("test")
internal class CourtCaseControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @MockitoBean val courtCaseService: CourtCaseService,
  @MockitoBean val auditService: AuditService,
) : DescribeSpec(
    {
      val hmppsId = "A1234AA"
      val path = "/v1/persons/$hmppsId/court-cases"
      val mockMvc = IntegrationAPIMockMvc(springMockMvc)

      describe("GET $path") {
        beforeTest {
          Mockito.reset(courtCaseService)
          Mockito.reset(auditService)
          whenever(courtCaseService.getCourtCaseDetails(any(), any())).thenReturn(
            Response(
              data = CourtCasesSummary(dateOfFirstConviction = LocalDate.of(2021, 2, 14), courtCode = "COURT2", courtOutcome = CourtOutcome(CourtOutComeType.SENTENCING, "Imprisonment")),
            ),
          )
        }

        it("logs audit for adjudications") {
          mockMvc.performAuthorised(path)
          verify(
            auditService,
            VerificationModeFactory.times(1),
          ).createEvent("COURT_CASES_SUMMARY", mapOf("hmppsId" to hmppsId))
        }

        it("returns a court cases summary") {
          val result = mockMvc.performAuthorised(path)
          result.response.contentAsString.shouldContainJsonKeyValue("$.data.dateOfFirstConviction", "2021-02-14")
          result.response.contentAsString.shouldContainJsonKeyValue("$.data.courtCode", "COURT2")
          result.response.contentAsString.shouldContainJsonKeyValue("$.data.courtOutcome.outcomeType", "SENTENCING")
          result.response.contentAsString.shouldContainJsonKeyValue("$.data.courtOutcome.outcomeName", "Imprisonment")
        }

        it("returns a 404") {
          whenever(courtCaseService.getCourtCaseDetails(any(), any())).thenReturn(
            Response(
              data = null,
              errors = listOf(UpstreamApiError(causedBy = UpstreamApi.REMAND_AND_SENTENCING, type = UpstreamApiError.Type.ENTITY_NOT_FOUND)),
            ),
          )
          val result = mockMvc.performAuthorised(path)
          result.response.status shouldBe 404
        }
      }
    },
  )
