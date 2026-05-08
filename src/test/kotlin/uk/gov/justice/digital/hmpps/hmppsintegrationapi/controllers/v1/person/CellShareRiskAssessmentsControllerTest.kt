package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.WebMvcTestConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.PrisonApiAssessmentSummary
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetCsraForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

@WebMvcTest(controllers = [CellShareRiskAssessmentController::class])
@Import(WebMvcTestConfiguration::class)
@ActiveProfiles("test")
internal class CellShareRiskAssessmentsControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @MockitoBean val csraService: GetCsraForPersonService,
  @MockitoBean val auditService: AuditService,
) : DescribeSpec(
    {
      val hmppsId = "A123456"
      val path = "/v1/persons/$hmppsId/cell-share-risk-assessments"
      val mockMvc = IntegrationAPIMockMvc(springMockMvc)
      val filters = null

      describe("GET $path") {
        beforeTest {
          Mockito.reset(csraService)
          whenever(csraService.getCsraAssessments(hmppsId, filters)).thenReturn(
            Response(
              listOf(
                PrisonApiAssessmentSummary(
                  bookingId = null,
                  assessmentSeq = null,
                  offenderNo = hmppsId,
                  classificationCode = null,
                  assessmentCode = "CSR",
                  cellSharingAlertFlag = true,
                  assessmentDate = null,
                  assessmentAgencyId = null,
                  assessmentComment = null,
                  assessorUser = null,
                  nextReviewDate = null,
                ),
              ),
            ),
          )
          Mockito.reset(auditService)
        }

        it("returns a 200 OK status code") {
          val result = mockMvc.performAuthorised(path)
          result.response.status.shouldBe(HttpStatus.OK.value())
        }

        it("gets csras for a person with the matching ID") {
          mockMvc.performAuthorised(path)
          verify(csraService, VerificationModeFactory.times(1)).getCsraAssessments(hmppsId, filters)
        }

        it("logs audit") {
          mockMvc.performAuthorised(path)

          verify(
            auditService,
            VerificationModeFactory.times(1),
          ).createEvent("GET_PERSON_CSRA", mapOf("hmppsId" to hmppsId))
        }

        it("returns a 404 NOT FOUND status code when person isn't found in the upstream API") {
          whenever(csraService.getCsraAssessments(hmppsId, filters)).thenReturn(
            Response(
              data = null,
              errors =
                listOf(
                  UpstreamApiError(
                    causedBy = UpstreamApi.PRISON_API,
                    type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                  ),
                ),
            ),
          )

          val result = mockMvc.performAuthorised(path)

          result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
        }

        it("returns a 400 BAD REQUEST status code when a bad request is sent to the upstream API") {
          whenever(csraService.getCsraAssessments(hmppsId, filters)).thenReturn(
            Response(
              data = null,
              errors =
                listOf(
                  UpstreamApiError(
                    causedBy = UpstreamApi.NDELIUS,
                    type = UpstreamApiError.Type.BAD_REQUEST,
                  ),
                ),
            ),
          )

          val result = mockMvc.performAuthorised(path)

          result.response.status.shouldBe(HttpStatus.BAD_REQUEST.value())
        }
      }
    },
  )
