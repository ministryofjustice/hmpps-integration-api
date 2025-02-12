package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonVisitRestriction
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetVisitRestrictionsForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

@WebMvcTest(controllers = [VisitRestrictionsController::class])
@ActiveProfiles("test")
internal class VisitRestrictionsControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @MockitoBean val getVisitRestrictionsForPersonService: GetVisitRestrictionsForPersonService,
  @MockitoBean val auditService: AuditService,
) : DescribeSpec(
    {
      val hmppsId = "A1234AA"
      val path = "/v1/persons/$hmppsId/visit-restrictions"
      val mockMvc = IntegrationAPIMockMvc(springMockMvc)

      beforeTest {
        Mockito.reset(getVisitRestrictionsForPersonService)
        Mockito.reset(auditService)
        whenever(getVisitRestrictionsForPersonService.execute(hmppsId, filters = null)).thenReturn(
          Response(
            data =
              listOf(
                PersonVisitRestriction(restrictionId = 1, comment = "Restriction 1", restrictionType = "TYPE", restrictionTypeDescription = "Type description", startDate = "2025-01-01", expiryDate = "2025-12-31", active = true),
                PersonVisitRestriction(restrictionId = 2, comment = "Restriction 2", restrictionType = "TYPE", restrictionTypeDescription = "Type description", startDate = "2025-01-01", expiryDate = "2025-12-31", active = true),
              ),
          ),
        )
      }

      it("returns 200 when successfully find visit restrictions") {
        val result = mockMvc.performAuthorised(path)
        result.response.status.shouldBe(HttpStatus.OK.value())
        result.response.contentAsString.shouldBe(
          """
          {
            "data": [
              {
                "restrictionId" : 1,
                "comment" : "Restriction 1",
                "restrictionType": "TYPE",
                "restrictionTypeDescription" : "Type description",
                "startDate" : "2025-01-01",
                "expiryDate" : "2025-12-31",
                "active" : true
              },
              {
                "restrictionId" : 2,
                "comment" : "Restriction 2",
                "restrictionType": "TYPE",
                "restrictionTypeDescription" : "Type description",
                "startDate" : "2025-01-01",
                "expiryDate" : "2025-12-31",
                "active" : true
              }
          ]
        }
        """.removeWhitespaceAndNewlines(),
        )
      }

      it("returned 404 when service returns entity not found") {
        whenever(getVisitRestrictionsForPersonService.execute(hmppsId)).thenReturn(
          Response(
            data = emptyList(),
            errors =
              listOf(
                UpstreamApiError(
                  type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                  causedBy = UpstreamApi.NOMIS,
                ),
              ),
          ),
        )
        val result = mockMvc.performAuthorised(path)
        result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
      }

      it("returned 400 when service returns bad request") {
        whenever(getVisitRestrictionsForPersonService.execute(hmppsId)).thenReturn(
          Response(
            data = emptyList(),
            errors =
              listOf(
                UpstreamApiError(
                  type = UpstreamApiError.Type.BAD_REQUEST,
                  causedBy = UpstreamApi.NOMIS,
                ),
              ),
          ),
        )
        val result = mockMvc.performAuthorised(path)
        result.response.status.shouldBe(HttpStatus.BAD_REQUEST.value())
      }

      it("logs audit for visit restrictions") {
        mockMvc.performAuthorised(path)

        verify(
          auditService,
          VerificationModeFactory.times(1),
        ).createEvent("GET_PERSON_VISIT_RESTRICTIONS", mapOf("hmppsId" to hmppsId))
      }
    },
  )
