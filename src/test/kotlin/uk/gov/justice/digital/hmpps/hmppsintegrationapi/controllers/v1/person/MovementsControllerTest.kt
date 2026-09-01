package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.PrisonerMovementsResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetMovementService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.MovementDiary
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetMovementsService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

@WebMvcTest(controllers = [MovementsController::class])
@Import(WebMvcTestConfiguration::class)
@ActiveProfiles("test")
internal class MovementsControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @MockitoBean val getMovementService: GetMovementsService,
  @MockitoBean val auditService: AuditService,
) : DescribeSpec(
    {
      val hmppsId = "A123456"
      val pathScheduled = "/v1/persons/$hmppsId/movements/scheduled"
      val pathTransfer = "/v1/persons/$hmppsId/movements/transfer-summary"
      val mockMvc = IntegrationAPIMockMvc(springMockMvc)

      describe("GET $pathScheduled") {
        beforeTest {
          Mockito.reset(getMovementService)
          whenever(getMovementService.getMovementsSummary(any(), any())).thenReturn(
            Response(
              listOf(
                MovementDiary(
                  "2026-08-27T00:33:28.896Z",
                  "PA",
                  "Comment",
                ),
              ),
            ),
          )
          Mockito.reset(auditService)
        }

        it("returns a 200 OK status code") {
          val result = mockMvc.performAuthorised(pathScheduled)
          result.response.status.shouldBe(HttpStatus.OK.value())
        }

        it("gets movements summary for a person with the matching ID") {
          mockMvc.performAuthorised(pathScheduled)
          verify(getMovementService, VerificationModeFactory.times(1)).getMovementsSummary(eq(hmppsId), any())
        }

        it("logs audit") {
          mockMvc.performAuthorised(pathScheduled)

          verify(
            auditService,
            VerificationModeFactory.times(1),
          ).createEvent("GET_MOVEMENTS_SUMMARY", mapOf("hmppsId" to hmppsId))
        }

        it("returns a 404 NOT FOUND status code when person isn't found in the upstream API") {
          whenever(getMovementService.getMovementsSummary(eq(hmppsId), any())).thenReturn(
            Response(
              data = listOf(),
              errors =
                listOf(
                  UpstreamApiError(
                    causedBy = UpstreamApi.PRISON_API,
                    type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                  ),
                ),
            ),
          )

          val result = mockMvc.performAuthorised(pathScheduled)

          result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
        }
      },
      describe("GET $pathTransfer") {
        beforeTest {
          Mockito.reset(getMovementService)
          whenever(getMovementService.getMovement(any(), any())).thenReturn(
            Response(
              PrisonerMovementsResponse(
                "Normal Transfer",
                "NOTR",
                "Moorland (HMP & YOI)",
                "Millsike (HMP)",
                "Millsike (HMP)",
                "2026-07-14T11:41:05Z",
                "Moorland (HMP & YOI)",
                "2026-08-14T12:41:05",
              ),
            ),
          )
          Mockito.reset(auditService)
        }

        it("returns a 200 OK status code") {
          val result = mockMvc.performAuthorised(pathTransfer)
          result.response.status.shouldBe(HttpStatus.OK.value())
        }

        it("gets movements summary for a person with the matching ID") {
          mockMvc.performAuthorised(pathTransfer)
          verify(getMovementService, VerificationModeFactory.times(1)).getMovement(eq(hmppsId), any())
        }

        it("logs audit") {
          mockMvc.performAuthorised(pathTransfer)

          verify(
            auditService,
            VerificationModeFactory.times(1),
          ).createEvent("GET_PERSON_MOVEMENT_SUMMARY", mapOf("hmppsId" to hmppsId))
        }

        it("returns a 404 NOT FOUND status code when person isn't found in the upstream API") {
          whenever(getMovementService.getMovement(eq(hmppsId), any())).thenReturn(
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

          val result = mockMvc.performAuthorised(pathTransfer)

          result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
        }
      }
    },
  )
