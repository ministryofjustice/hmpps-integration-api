package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds.GeneralPredictorScore as ArnGeneralPredictorScore
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds.RiskPredictors as ArnRiskPredictors

class RiskPredictorsTest : DescribeSpec(
  {
    describe("#toRiskPredictors") {
      it("maps ARN Risk Predictors to integration API Risk Predictors") {
        val arnRiskPredictors = ArnRiskPredictors(
          generalPredictorScore = ArnGeneralPredictorScore(ogpTotalWeightedScore = 80),
        )

        val integrationApiRiskPredictors = arnRiskPredictors.toRiskPredictors()

        integrationApiRiskPredictors.generalPredictorScore.totalWeightedScore.shouldBe(
          arnRiskPredictors.generalPredictorScore.ogpTotalWeightedScore,
        )
      }
    }
  },
)
