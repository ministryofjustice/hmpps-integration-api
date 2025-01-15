package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class GeneralPredictorScoreTest :
  DescribeSpec(
    {
      describe("#toGeneralPredictor") {
        it("maps one-to-one attributes to integration API attributes") {

          val arnGeneralPredictorScore =
            ArnGeneralPredictorScore(
              ogpRisk = "HIGH",
            )

          val integrationApiGeneralPredictor = arnGeneralPredictorScore.toGeneralPredictor()

          integrationApiGeneralPredictor.scoreLevel.shouldBe(arnGeneralPredictorScore.ogpRisk)
        }
      }
    },
  )
