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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import java.time.LocalDateTime
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.GeneralPredictor as HmppsGeneralPredictor
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.GroupReconviction as HmppsGroupReconviction
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.RiskOfSeriousRecidivism as HmppsRiskOfSeriousRecidivism
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.RiskPredictorScore as HmppsRiskPredictorScore
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.SexualPredictor as HmppsSexualPredictor
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ViolencePredictor as HmppsViolencePredictor

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
      val deliusCrn = "X777776"

      beforeEach {
        assessRisksAndNeedsApiMockServer.start()
        Mockito.reset(hmppsAuthGateway)
        assessRisksAndNeedsApiMockServer.stubGetRiskPredictorScoresForPerson(
          deliusCrn,
          """
            [
              {
                "completedDate": "2023-09-05T10:15:41",
                "assessmentStatus": "COMPLETE",
                "groupReconvictionScore": {
                      "scoreLevel": "HIGH"
                  },
                "generalPredictorScore": {
                      "ogpRisk": "LOW"
                  },
                "violencePredictorScore": {
                      "ovpRisk": "MEDIUM"
                  },
                "riskOfSeriousRecidivismScore": {
                      "scoreLevel": "VERY_HIGH"
                  },
                "sexualPredictorScore": {
                      "ospIndecentScoreLevel": "HIGH",
                      "ospContactScoreLevel": "VERY_HIGH"
                  }
              }
            ]
          """,
        )

        Mockito.reset(hmppsAuthGateway)
        whenever(hmppsAuthGateway.getClientToken("ASSESS_RISKS_AND_NEEDS")).thenReturn(HmppsAuthMockServer.TOKEN)
      }

      afterTest {
        assessRisksAndNeedsApiMockServer.stop()
      }

      it("authenticates using HMPPS Auth with credentials") {
        assessRisksAndNeedsGateway.getRiskPredictorScoresForPerson(deliusCrn)

        verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("ASSESS_RISKS_AND_NEEDS")
      }

      it("returns risk predictor scores for the matching CRN") {
        val response = assessRisksAndNeedsGateway.getRiskPredictorScoresForPerson(deliusCrn)
        response.data.shouldBe(
          listOf(
            HmppsRiskPredictorScore(
              completedDate = LocalDateTime.parse("2023-09-05T10:15:41"),
              assessmentStatus = "COMPLETE",
              groupReconviction = HmppsGroupReconviction(scoreLevel = "HIGH"),
              generalPredictor = HmppsGeneralPredictor(scoreLevel = "LOW"),
              violencePredictor = HmppsViolencePredictor(scoreLevel = "MEDIUM"),
              riskOfSeriousRecidivism = HmppsRiskOfSeriousRecidivism(scoreLevel = "VERY_HIGH"),
              sexualPredictor = HmppsSexualPredictor(indecentScoreLevel = "HIGH", contactScoreLevel = "VERY_HIGH"),
            ),
          ),
        )
      }

      it("returns an empty list when no risk predictor scores are found") {
        assessRisksAndNeedsApiMockServer.stubGetRiskPredictorScoresForPerson(deliusCrn, "[]")

        val response = assessRisksAndNeedsGateway.getRiskPredictorScoresForPerson(deliusCrn)

        response.data.shouldBe(emptyList())
      }

      it("returns a 404 NOT FOUND status code when no person is found") {
        assessRisksAndNeedsApiMockServer.stubGetRiskPredictorScoresForPerson(deliusCrn, "", HttpStatus.NOT_FOUND)

        val response = assessRisksAndNeedsGateway.getRiskPredictorScoresForPerson(deliusCrn)

        response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND).shouldBeTrue()
      }
    },
  )
