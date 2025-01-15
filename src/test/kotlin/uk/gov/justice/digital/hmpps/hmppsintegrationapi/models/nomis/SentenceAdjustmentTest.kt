package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class SentenceAdjustmentTest :
  DescribeSpec(
    {
      describe("#toSentenceAdjustment") {
        it("maps one-to-one attributes to integration API attributes") {
          val sentenceAdjustmentFromNomis =
            NomisSentenceAdjustment(
              additionalDaysAwarded = 12,
              unlawfullyAtLarge = 10,
              lawfullyAtLarge = 2,
              restoredAdditionalDaysAwarded = 0,
              specialRemission = 11,
              recallSentenceRemand = 1,
              recallSentenceTaggedBail = 3,
              remand = 6,
              taggedBail = 3,
              unusedRemand = 6,
            )

          val sentenceAdjustment = sentenceAdjustmentFromNomis.toSentenceAdjustment()

          sentenceAdjustment.additionalDaysAwarded.shouldBe(12)
          sentenceAdjustment.unlawfullyAtLarge.shouldBe(10)
          sentenceAdjustment.lawfullyAtLarge.shouldBe(2)
          sentenceAdjustment.restoredAdditionalDaysAwarded.shouldBe(0)
          sentenceAdjustment.specialRemission.shouldBe(11)
          sentenceAdjustment.recallSentenceRemand.shouldBe(1)
          sentenceAdjustment.recallSentenceTaggedBail.shouldBe(3)
          sentenceAdjustment.remand.shouldBe(6)
          sentenceAdjustment.taggedBail.shouldBe(3)
          sentenceAdjustment.unusedRemand.shouldBe(6)
        }
      }
    },
  )
