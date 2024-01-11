package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
class RiskOfSeriousRecidivismScoreTest : DescribeSpec(
  {
    describe("#toRiskOfSeriousRecidivism") {
      it("maps one-to-one attributes to integration API attributes") {

        val arnRiskOfSeriousRecidivismScore = ArnRiskOfSeriousRecidivismScore(
          scoreLevel = "HIGH",
        )

        val integrationApiRiskOfSeriousRecidivism = arnRiskOfSeriousRecidivismScore.toRiskOfSeriousRecidivism()

        integrationApiRiskOfSeriousRecidivism.scoreLevel.shouldBe(arnRiskOfSeriousRecidivismScore.scoreLevel)
      }
    }
  },
)
