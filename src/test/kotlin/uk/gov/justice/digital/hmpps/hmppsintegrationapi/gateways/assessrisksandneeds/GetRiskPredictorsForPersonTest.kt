package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.assessrisksandneeds

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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.AssessRisksAndNeedsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.AssessRisksAndNeedsApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [AssessRisksAndNeedsGateway::class],
)
class GetRiskPredictorsForPersonTest(
  @MockBean val hmppsAuthGateway: HmppsAuthGateway,
  val assessRisksAndNeedsGateway: AssessRisksAndNeedsGateway,
) :
  DescribeSpec(
    {
      val assessRisksAndNeedsApiMockServer = AssessRisksAndNeedsApiMockServer()
      val crn = "X000000"

      beforeEach {
        assessRisksAndNeedsApiMockServer.start()
        Mockito.reset(hmppsAuthGateway)
        assessRisksAndNeedsApiMockServer.stubGetRiskPredictorsForPerson(
          crn,
          """
            [
              {
                "generalPredictorScore": {
                  "ogpTotalWeightedScore": 0
                }
              }
            ]
          """,
        )

        whenever(hmppsAuthGateway.getClientToken("ARN")).thenReturn(HmppsAuthMockServer.TOKEN)
      }

      afterTest {
        assessRisksAndNeedsApiMockServer.stop()
      }

      it("authenticates using HMPPS Auth with credentials") {
        assessRisksAndNeedsGateway.getRiskPredictorsForPerson(crn)

        verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("ARN")
      }

      it("returns risk predictors for the matching CRN") {
        val response = assessRisksAndNeedsGateway.getRiskPredictorsForPerson(crn)

        response.data.shouldBe(
          """
            [
              {
                "generalPredictorScore": {
                  "ogpTotalWeightedScore": 0
                }
              }
            ]
          """,
        )
      }
    },
  )
