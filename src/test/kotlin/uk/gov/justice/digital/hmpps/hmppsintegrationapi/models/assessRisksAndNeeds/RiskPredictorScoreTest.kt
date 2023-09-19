package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDateTime
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds.GeneralPredictorScore as ArnGeneralPredictorScore
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds.GroupReconvictionScore as ArnGroupConvictionScore
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds.RiskPredictorScore as ArnRiskPredictorScore
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds.ViolencePredictorScore as ArnViolencePredictorScore

class RiskPredictorScoreTest : DescribeSpec(
  {
    describe("#toRiskPredictorScore") {
      it("maps ARN Risk Predictor Score to integration API Risk Predictor Score") {
        val arnRiskPredictorScore = ArnRiskPredictorScore(
          completedDate = "2023-09-05T10:15:41",
          assessmentStatus = "COMPLETE",
          generalPredictorScore = ArnGeneralPredictorScore(ogpRisk = "MEDIUM"),
          violencePredictorScore = ArnViolencePredictorScore(ovpRisk = "LOW"),
          groupReconvictionScore = ArnGroupConvictionScore(scoreLevel = "VERY_HIGH"),
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
      }
    }
  },
)
