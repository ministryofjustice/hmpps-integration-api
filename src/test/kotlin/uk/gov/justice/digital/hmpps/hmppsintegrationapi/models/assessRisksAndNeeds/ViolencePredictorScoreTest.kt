package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds.ViolencePredictorScore as ArnViolencePredictorScore
class ViolencePredictorScoreTest : DescribeSpec(
  {
    describe("#toViolencePredictorScore") {
      it("maps one-to-one attributes to integration API attributes") {
        val arnViolencePredictorScore = ArnViolencePredictorScore(
          ovpRisk = "VERY_HIGH",
        )

        val integrationApiViolencePredictorScore = arnViolencePredictorScore.toViolencePredictorScore()

        integrationApiViolencePredictorScore.ovpRisk.shouldBe(arnViolencePredictorScore.ovpRisk)
      }
    }
  },
)
