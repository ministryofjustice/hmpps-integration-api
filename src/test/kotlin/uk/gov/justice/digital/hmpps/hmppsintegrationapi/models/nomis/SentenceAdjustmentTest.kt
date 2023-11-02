package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis.SentenceAdjustment as SentenceAdjustmentFromNomis

class SentenceAdjustmentTest : DescribeSpec(
  {
    describe("#toSentenceAdjustment") {
      it("maps one-to-one attributes to integration API attributes") {
        val sentenceAdjustmentFromNomis = SentenceAdjustmentFromNomis(
          additionalDaysAwarded = 12,
        )

        val sentenceAdjustment = sentenceAdjustmentFromNomis.toSentenceAdjustment()

        sentenceAdjustment.additionalDaysAwarded.shouldBe(12)
      }
    }
  },
)
