package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.riskManagement

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.RiskManagementGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.RiskManagementApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import java.io.File

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [RiskManagementGateway::class],
)
class RiskManagementGatewayTest(
  @MockBean val hmppsAuthGateway: HmppsAuthGateway,
  private val riskManagementGateway: RiskManagementGateway,
) : DescribeSpec({

    val riskManagementMockServer = RiskManagementApiMockServer()

    beforeEach {
      riskManagementMockServer.start()
      Mockito.reset(hmppsAuthGateway)

      whenever(hmppsAuthGateway.getClientToken("Risk Management Plan Search")).thenReturn(HmppsAuthMockServer.TOKEN)
    }

    afterTest {
      riskManagementMockServer.stop()
    }

    describe("Get risks for given CRN") {
      val crn = "D1974X"

      beforeEach {
        riskManagementMockServer.stubGetRiskManagementPlan(
          crn,
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
    }
  })
