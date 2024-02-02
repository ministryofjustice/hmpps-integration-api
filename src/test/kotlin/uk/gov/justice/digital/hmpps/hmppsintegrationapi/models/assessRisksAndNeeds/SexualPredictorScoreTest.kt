package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class SexualPredictorScoreTest : DescribeSpec(
  {
    describe("#toSexualPredictor") {
      it("maps one-to-one attributes to integration API attributes") {
        val arnSexualPredictorScore = ArnSexualPredictorScore(
          ospIndecentScoreLevel = "HIGH",
          ospContactScoreLevel = "VERY_HIGH",
        )

        val integrationApiSexualPredictor = arnSexualPredictorScore.toSexualPredictor()

        integrationApiSexualPredictor.indecentScoreLevel.shouldBe(arnSexualPredictorScore.ospIndecentScoreLevel)
        integrationApiSexualPredictor.contactScoreLevel.shouldBe(arnSexualPredictorScore.ospContactScoreLevel)
      }
    }
  },
)
