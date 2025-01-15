package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class ViolencePredictorScoreTest :
  DescribeSpec(
    {
      describe("#toViolencePredictor") {
        it("maps one-to-one attributes to integration API attributes") {
          val arnViolencePredictorScore =
            ArnViolencePredictorScore(
              ovpRisk = "VERY_HIGH",
            )

          val integrationApiViolencePredictor = arnViolencePredictorScore.toViolencePredictor()

          integrationApiViolencePredictor.scoreLevel.shouldBe(arnViolencePredictorScore.ovpRisk)
        }
      }
    },
  )
