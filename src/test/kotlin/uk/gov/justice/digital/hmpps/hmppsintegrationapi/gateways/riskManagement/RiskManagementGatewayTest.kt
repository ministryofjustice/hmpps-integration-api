package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.riskManagement

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.ResponseException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.RiskManagementGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import java.io.File

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [RiskManagementGateway::class],
)
class RiskManagementGatewayTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  @MockitoBean val featureFlag: FeatureFlagConfig,
  private val riskManagementGateway: RiskManagementGateway,
) : DescribeSpec({

    val riskManagementMockServer = ApiMockServer.create(UpstreamApi.RISK_MANAGEMENT_PLAN)

    beforeEach {
      riskManagementMockServer.start()
      Mockito.reset(hmppsAuthGateway)
      whenever(featureFlag.useArnsEndpoints).thenReturn(true)

      whenever(hmppsAuthGateway.getClientToken("Risk Management Plan Search")).thenReturn(HmppsAuthMockServer.TOKEN)
    }

    afterTest {
      riskManagementMockServer.stop()
    }

    describe("Get risks for given CRN") {
      val crn = "D1974X"
      val path = "/risks/risk-management-plan/$crn"

      beforeEach {
        riskManagementMockServer.stubForGet(
          path,
          File(
            "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/riskManagement/fixtures/GetRiskManagementPlanResponse.json",
          ).readText(),
        )
      }

      it("authenticates using HMPPS Auth with credentials") {
        riskManagementGateway.getRiskManagementPlansForCrn(crn)
        verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("Risk Management Plan Search")
      }

      it("returns a risk management plan when searching with a valid CRN") {
        val response = riskManagementGateway.getRiskManagementPlansForCrn(crn)
        val riskPlan = response.data
        riskPlan?.crn.shouldBe(crn)
        riskPlan?.riskManagementPlan?.size.shouldBe(1)
        response.errors.size.shouldBe(0)
      }

      it("returns an error response when searching with an invalid CRN") {
        val response = riskManagementGateway.getRiskManagementPlansForCrn("Not a valid CRN")
        val riskPlan = response.data
        riskPlan.shouldBe(null)
        response.errors.size.shouldBe(1)
        response.errors[0].type.shouldBe(UpstreamApiError.Type.ENTITY_NOT_FOUND)
      }

      it("returns an error response when upstream service returns 403") {
        riskManagementMockServer.stubForGet(
          path,
          "{}",
          HttpStatus.FORBIDDEN,
        )

        val response = riskManagementGateway.getRiskManagementPlansForCrn(crn)
        response.errors.size.shouldBe(1)
        response.errors[0].type.shouldBe(UpstreamApiError.Type.FORBIDDEN)
      }

      it("returns 503 service not available when feature flag set to false") {
        whenever(featureFlag.useArnsEndpoints).thenReturn(false)
        val exception = shouldThrow<ResponseException> { riskManagementGateway.getRiskManagementPlansForCrn(crn) }
        exception.shouldBe(ResponseException("use-arns-endpoints not enabled", 503))
      }
    }
  })
