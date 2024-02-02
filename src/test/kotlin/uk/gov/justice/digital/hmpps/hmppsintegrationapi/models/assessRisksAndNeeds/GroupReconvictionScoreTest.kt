package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class GroupReconvictionScoreTest : DescribeSpec(
  {
    describe("#toGroupReconviction") {
      it("maps one-to-one attributes to integration API attributes") {

        val arnGroupReconvictionScore = ArnGroupReconvictionScore(
          scoreLevel = "VERY_HIGH",
        )

        val integrationApiGroupReconviction = arnGroupReconvictionScore.toGroupReconviction()

        integrationApiGroupReconviction.scoreLevel.shouldBe(arnGroupReconvictionScore.scoreLevel)
      }
    }
  },
)
