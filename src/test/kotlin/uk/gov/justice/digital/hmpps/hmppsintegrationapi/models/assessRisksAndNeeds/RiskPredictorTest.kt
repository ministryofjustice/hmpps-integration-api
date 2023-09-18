package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds.GeneralPredictorScore as ArnGeneralPredictorScore
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds.RiskPredictorScore as ArnRiskPredictorScore

class RiskPredictorTest : DescribeSpec(
  {
    describe("#toRiskPredictorScore") {
      it("maps ARN Risk Predictor Score to integration API Risk Predictor Score") {
        val arnRiskPredictorScore = ArnRiskPredictorScore(
          generalPredictorScore = ArnGeneralPredictorScore(ogpRisk = "MEDIUM"),
        )

        val integrationApiRiskPredictorScore = arnRiskPredictorScore.toRiskPredictorScore()

        integrationApiRiskPredictorScore.generalPredictorScore.ogpRisk.shouldBe(
          arnRiskPredictorScore.generalPredictorScore.ogpRisk,
        )
      }
    }
  },
)
