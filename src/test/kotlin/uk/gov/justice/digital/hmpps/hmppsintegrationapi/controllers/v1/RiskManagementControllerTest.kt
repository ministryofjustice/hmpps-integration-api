package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1

import io.kotest.assertions.json.shouldContainJsonKeyValue
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.RiskManagementPlan
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetRiskManagementPlansForCrnService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@WebMvcTest(controllers = [RiskManagementController::class])
@ActiveProfiles("test")
class RiskManagementControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @MockitoBean val getRiskManagementService: GetRiskManagementPlansForCrnService,
  @MockitoBean val auditService: AuditService,
) : DescribeSpec({
    val hmppsId = "D1974X"
    val badHmppsId = "Not a real CRN"
    val encodedHmppsId = URLEncoder.encode(hmppsId, StandardCharsets.UTF_8)
    val encodedBadHmppsId = URLEncoder.encode(badHmppsId, StandardCharsets.UTF_8)
    val basePath = "/v1/persons/hmppsId/risk-management-plan"
    val mockMvc = IntegrationAPIMockMvc(springMockMvc)

    describe("GET $basePath") {
      beforeTest {
        Mockito.reset(getRiskManagementService)
        Mockito.reset(auditService)
        whenever(getRiskManagementService.execute(hmppsId)).thenReturn(
          Response(
            data =
              listOf(
                RiskManagementPlan(
                  assessmentId = "123450",
                  dateCompleted = "2024-05-04T01 =04 =20",
                  initiationDate = "2024-05-04T01 =04 =20",
                  assessmentStatus = "string",
                  assessmentType = "string",
                  keyInformationCurrentSituation = "string",
                  furtherConsiderationsCurrentSituation = "string",
                  supervision = "string",
                  monitoringAndControl = "string",
                  interventionsAndTreatment = "string",
                  victimSafetyPlanning = "string",
                  latestSignLockDate = "2024-05-04T01 =04 =20",
                  latestCompleteDate = "2024-05-04T01 =04 =20",
                  contingencyPlans = "some",
                ),
                RiskManagementPlan(
                  assessmentId = "123451",
                  dateCompleted = "2024-05-04T01 =04 =20",
                  initiationDate = "2024-05-04T01 =04 =20",
                  assessmentStatus = "string",
                  assessmentType = "string",
                  keyInformationCurrentSituation = "string",
                  furtherConsiderationsCurrentSituation = "string",
                  supervision = "string",
                  monitoringAndControl = "string",
                  interventionsAndTreatment = "string",
                  victimSafetyPlanning = "string",
                  latestSignLockDate = "2024-05-04T01 =04 =20",
                  latestCompleteDate = "2024-05-04T01 =04 =20",
                  contingencyPlans = "some",
                ),
              ),
          ),
        )
        whenever(getRiskManagementService.execute(badHmppsId)).thenReturn(
          Response(
            null,
            errors =
              listOf(
                UpstreamApiError(
                  type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                  causedBy = UpstreamApi.RISK_MANAGEMENT_PLAN,
                  description = "Mock Error",
                ),
              ),
          ),
        )
      }

      it("Returns 200 OK") {
        val result = mockMvc.performAuthorised("/v1/persons/$encodedHmppsId/risk-management-plan")
        result.response.status.shouldBe(HttpStatus.OK.value())
      }

      it("Gets a risk management plan") {
        mockMvc.performAuthorised("/v1/persons/$encodedHmppsId/risk-management-plan")
        verify(getRiskManagementService, times(1)).execute(hmppsId)
      }

      it("Gets an error if provided ID has no risk management plan") {
        val result = mockMvc.performAuthorised("/v1/persons/$encodedBadHmppsId/risk-management-plan")
        verify(getRiskManagementService, times(1)).execute(badHmppsId)
        assert(result.response.status == 404)
      }

      it("logs audit") {
        mockMvc.performAuthorised("/v1/persons/$encodedHmppsId/risk-management-plan")
        verify(
          auditService,
          VerificationModeFactory.times(1),
        ).createEvent(
          "GET_RISK_MANAGEMENT_PLANS",
          mapOf("hmppsId" to hmppsId),
        )
      }

      it("returns paginated result") {
        val result = mockMvc.performAuthorised("/v1/persons/$encodedHmppsId/risk-management-plan")

        result.response.contentAsString.shouldContainJsonKeyValue("$.pagination.page", 1)
        result.response.contentAsString.shouldContainJsonKeyValue("$.pagination.totalPages", 1)
      }
    }

    describe("GET risk management plans returns Internal Server Error when Upstream api throws an unexpected error") {
      beforeTest {
        Mockito.reset(getRiskManagementService)
        Mockito.reset(auditService)
      }

      it("fails with the appropriate error when an upstream service is down") {
        whenever(getRiskManagementService.execute(any())).doThrow(
          WebClientResponseException(500, "MockError", null, null, null, null),
        )

        val response =
          mockMvc.performAuthorised(
            "/v1/persons/$encodedHmppsId/risk-management-plan",
          )

        assert(response.response.status == 500)
        assert(
          response.response.contentAsString.equals(
            "{\"status\":500,\"errorCode\":null,\"userMessage\":\"500 MockError\",\"developerMessage\":\"Unable to complete request as an upstream service is not responding\",\"moreInfo\":null}",
          ),
        )
      }
    }
  })
