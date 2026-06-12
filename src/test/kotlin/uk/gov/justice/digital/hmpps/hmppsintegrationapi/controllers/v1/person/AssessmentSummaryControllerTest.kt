package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.WebMvcTestConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.AssessmentSummary
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.AssessmentSummaryService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService
import java.time.LocalDateTime

@WebMvcTest(controllers = [AssessmentSummaryController::class])
@Import(WebMvcTestConfiguration::class)
@ActiveProfiles("test")
internal class AssessmentSummaryControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @MockitoBean val assessmentSummaryService: AssessmentSummaryService,
  @MockitoBean val auditService: AuditService,
) : DescribeSpec(
    {
      val hmppsId = "AA1111A"
      val path = "/v1/persons/$hmppsId/assessment-summary"
      val mockMvc = IntegrationAPIMockMvc(springMockMvc)
      val assessmentSummary =
        AssessmentSummary(
          initiationDate = LocalDateTime.of(2026, 1, 16, 16, 22, 54),
          completedDate = LocalDateTime.of(2026, 2, 5, 9, 13, 21),
          assessmentType = "Test Assessment Type",
          status = "Test Assessment Status",
          assessorName = "Test Assessor Name",
          countersignerName = "Test Countersigner Name",
        )

      describe("GET $path") {
        beforeTest {
          Mockito.reset(assessmentSummaryService)
          whenever(assessmentSummaryService.assessmentSummary(hmppsId)).thenReturn(Response(assessmentSummary))

          Mockito.reset(auditService)
        }

        it("returns a 200 OK status code") {
          val result = mockMvc.performAuthorised(path)
          result.response.status.shouldBe(HttpStatus.OK.value())
          result.response.contentAsString.shouldContain(
            """
          "data": {
            "initiationDate": "2026-01-16T16:22:54",
            "completedDate": "2026-02-05T09:13:21",
            "assessmentType": "Test Assessment Type",
            "status": "Test Assessment Status",
            "assessorName": "Test Assessor Name",
            "countersignerName": "Test Countersigner Name"
          }
        """.removeWhitespaceAndNewlines(),
          )
        }

        it("returns a 404 NOT FOUND status") {
          whenever(assessmentSummaryService.assessmentSummary(hmppsId)).thenReturn(Response(null, listOf(UpstreamApiError(causedBy = UpstreamApi.ASSESS_RISKS_AND_NEEDS, type = UpstreamApiError.Type.ENTITY_NOT_FOUND))))
          val result = mockMvc.performAuthorised(path)
          result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
        }
      }
    },
  )
