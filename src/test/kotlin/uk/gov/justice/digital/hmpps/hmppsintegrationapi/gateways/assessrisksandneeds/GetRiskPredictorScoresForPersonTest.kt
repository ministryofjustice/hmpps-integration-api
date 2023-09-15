package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.assessrisksandneeds

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.AssessRisksAndNeedsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.AssessRisksAndNeedsApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.GeneralPredictorScore
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.RiskPredictorScore as IntegrationAPIRiskPredictorScore

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [AssessRisksAndNeedsGateway::class],
)
class GetRiskPredictorScoresForPersonTest(
  @MockBean val hmppsAuthGateway: HmppsAuthGateway,
  val assessRisksAndNeedsGateway: AssessRisksAndNeedsGateway,
) :
  DescribeSpec(
    {
      val assessRisksAndNeedsApiMockServer = AssessRisksAndNeedsApiMockServer()
      val crn = "X777776"

      beforeEach {
        assessRisksAndNeedsApiMockServer.start()
        Mockito.reset(hmppsAuthGateway)
        assessRisksAndNeedsApiMockServer.stubGetRiskPredictorScoresForPerson(
          crn,
          """
            [
                {
                    "generalPredictorScore": {
                        "ogpRisk": 0
                    }
                }
            ]
          """,
        )

        Mockito.reset(hmppsAuthGateway)
        whenever(hmppsAuthGateway.getClientToken("ARN")).thenReturn(HmppsAuthMockServer.TOKEN)
      }

      afterTest {
        assessRisksAndNeedsApiMockServer.stop()
      }

      it("authenticates using HMPPS Auth with credentials") {
        assessRisksAndNeedsGateway.getRiskPredictorScoresForPerson(crn)

        verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("ARN")
      }

      it("returns risk predictor scores for the matching CRN") {
        val response = assessRisksAndNeedsGateway.getRiskPredictorScoresForPerson(crn)
        response.data.shouldBe(
          listOf(IntegrationAPIRiskPredictorScore(generalPredictorScore = GeneralPredictorScore(ogpRisk = 0))),
        )
      }

      it("returns an empty list when no risk predictor scores are found") {
        assessRisksAndNeedsApiMockServer.stubGetRiskPredictorScoresForPerson(crn, "[]")

        val response = assessRisksAndNeedsGateway.getRiskPredictorScoresForPerson(crn)

        response.data.shouldBe(emptyList())
      }

      it("returns an error when 404 NOT FOUND is returned because no person is found") {
        assessRisksAndNeedsApiMockServer.stubGetRiskPredictorScoresForPerson(crn, "", HttpStatus.NOT_FOUND)

        val response = assessRisksAndNeedsGateway.getRiskPredictorScoresForPerson(crn)

        response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND).shouldBeTrue()
      }
    },
  )
