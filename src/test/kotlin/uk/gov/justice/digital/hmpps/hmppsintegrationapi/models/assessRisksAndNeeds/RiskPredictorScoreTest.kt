package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDateTime

class RiskPredictorScoreTest : DescribeSpec(
  {
    describe("#toRiskPredictorScore") {
      it("maps ARN Risk Predictor Score to integration API Risk Predictor Score") {
        val arnRiskPredictorScore = ArnRiskPredictorScore(
          completedDate = "2023-09-05T10:15:41",
          assessmentStatus = "COMPLETE",
          generalPredictorScore = ArnGeneralPredictorScore(ogpRisk = "MEDIUM"),
          violencePredictorScore = ArnViolencePredictorScore(ovpRisk = "LOW"),
          groupReconvictionScore = ArnGroupReconvictionScore(scoreLevel = "VERY_HIGH"),
          riskOfSeriousRecidivismScore = ArnRiskOfSeriousRecidivismScore(scoreLevel = "HIGH"),
          sexualPredictorScore = ArnSexualPredictorScore(ospIndecentScoreLevel = "HIGH", ospContactScoreLevel = "VERY_HIGH"),
        )

        val integrationApiRiskPredictorScore = arnRiskPredictorScore.toRiskPredictorScore()

        integrationApiRiskPredictorScore.completedDate.shouldBe(LocalDateTime.parse(arnRiskPredictorScore.completedDate))
        integrationApiRiskPredictorScore.assessmentStatus.shouldBe(arnRiskPredictorScore.assessmentStatus)
        integrationApiRiskPredictorScore.generalPredictor.scoreLevel.shouldBe(
          arnRiskPredictorScore.generalPredictorScore.ogpRisk,
        )
        integrationApiRiskPredictorScore.violencePredictor.scoreLevel.shouldBe(
          arnRiskPredictorScore.violencePredictorScore.ovpRisk,
        )
        integrationApiRiskPredictorScore.groupReconviction.scoreLevel.shouldBe(
          arnRiskPredictorScore.groupReconvictionScore.scoreLevel,
        )
        integrationApiRiskPredictorScore.riskOfSeriousRecidivism.scoreLevel.shouldBe(
          arnRiskPredictorScore.riskOfSeriousRecidivismScore.scoreLevel,
        )
        integrationApiRiskPredictorScore.sexualPredictor.indecentScoreLevel.shouldBe(
          arnRiskPredictorScore.sexualPredictorScore.ospIndecentScoreLevel,
        )
        integrationApiRiskPredictorScore.sexualPredictor.contactScoreLevel.shouldBe(
          arnRiskPredictorScore.sexualPredictorScore.ospContactScoreLevel,
        )
      }
    }
  },
)
