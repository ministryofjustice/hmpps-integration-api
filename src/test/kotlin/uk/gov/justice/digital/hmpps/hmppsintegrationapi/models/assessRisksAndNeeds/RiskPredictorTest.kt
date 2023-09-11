package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds.GeneralPredictorScore as ArnGeneralPredictorScore
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds.RiskPredictor as ArnRiskPredictor

class RiskPredictorTest : DescribeSpec(
  {
    describe("#toRiskPredictor") {
      it("maps ARN Risk Predictor to integration API Risk Predictor") {
        val arnRiskPredictor = ArnRiskPredictor(
          generalPredictorScore = ArnGeneralPredictorScore(ogpTotalWeightedScore = 80),
        )

        val integrationApiRiskPredictor = arnRiskPredictor.toRiskPredictor()

        integrationApiRiskPredictor.generalPredictorScore.totalWeightedScore.shouldBe(
          arnRiskPredictor.generalPredictorScore.ogpTotalWeightedScore,
        )
      }
    }
  },
)
