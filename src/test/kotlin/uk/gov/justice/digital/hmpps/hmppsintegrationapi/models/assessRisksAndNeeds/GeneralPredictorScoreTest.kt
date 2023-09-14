package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds.GeneralPredictorScore as ArnGeneralPredictorScore
class GeneralPredictorScoreTest : DescribeSpec(
  {
    describe("#toGeneralPredictorScore") {
      it("maps one-to-one attributes to integration API attributes") {

        val arnGeneralPredictorScore = ArnGeneralPredictorScore(
          ogpRisk = 70,
        )

        val integrationApiGeneralPredictorScore = arnGeneralPredictorScore.toGeneralPredictorScore()

        integrationApiGeneralPredictorScore.ogpRisk.shouldBe(arnGeneralPredictorScore.ogpRisk)
      }
    }
  },
)
