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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApiError
import java.time.LocalDateTime
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.GeneralPredictor as IntegrationAPIGeneralPredictor
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.GroupReconviction as IntegrationAPIGroupReconviction
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.RiskOfSeriousRecidivism as IntegrationAPIRiskOfSeriousRecidivism
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.RiskPredictorScore as IntegrationAPIRiskPredictorScore
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.SexualPredictor as IntegrationAPISexualPredictor
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ViolencePredictor as IntegrationAPIViolencePredictor

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
            IntegrationAPIRiskPredictorScore(
              completedDate = LocalDateTime.parse("2023-09-05T10:15:41"),
              assessmentStatus = "COMPLETE",
              groupReconviction = IntegrationAPIGroupReconviction(scoreLevel = "HIGH"),
              generalPredictor = IntegrationAPIGeneralPredictor(scoreLevel = "LOW"),
              violencePredictor = IntegrationAPIViolencePredictor(scoreLevel = "MEDIUM"),
              riskOfSeriousRecidivism = IntegrationAPIRiskOfSeriousRecidivism(scoreLevel = "VERY_HIGH"),
              sexualPredictor = IntegrationAPISexualPredictor(indecentScoreLevel = "HIGH", contactScoreLevel = "VERY_HIGH"),
            ),
          ),
        )
      }

      it("returns an empty list when no risk predictor scores are found") {
        assessRisksAndNeedsApiMockServer.stubGetRiskPredictorScoresForPerson(deliusCrn, "[]")

        val response = assessRisksAndNeedsGateway.getRiskPredictorScoresForPerson(deliusCrn)

        response.data.shouldBe(emptyList())
      }

      it("returns an error when 404 NOT FOUND is returned because no person is found") {
        assessRisksAndNeedsApiMockServer.stubGetRiskPredictorScoresForPerson(deliusCrn, "", HttpStatus.NOT_FOUND)

        val response = assessRisksAndNeedsGateway.getRiskPredictorScoresForPerson(deliusCrn)

        response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND).shouldBeTrue()
      }
    },
  )
