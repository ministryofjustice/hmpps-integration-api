package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.SentenceAdjustment

data class SentenceAdjustment(
  val additionalDaysAwarded: Number? = null,
  val unlawfullyAtLarge: Number? = null,
  val lawfullyAtLarge: Number? = null,
  val restoredAdditionalDaysAwarded: Number? = null,
  val specialRemission: Number? = null,
  val recallSentenceRemand: Number? = null,
  val recallSentenceTaggedBail: Number? = null,
  val remand: Number? = null,
  val taggedBail: Number? = null,
  val unusedRemand: Number? = null,
) {
  fun toSentenceAdjustment(): SentenceAdjustment = SentenceAdjustment(
    additionalDaysAwarded = this.additionalDaysAwarded,
    unlawfullyAtLarge = this.unlawfullyAtLarge,
    lawfullyAtLarge = this.lawfullyAtLarge,
    restoredAdditionalDaysAwarded = this.restoredAdditionalDaysAwarded,
    specialRemission = this.specialRemission,
    recallSentenceRemand = this.recallSentenceRemand,
    recallSentenceTaggedBail = this.recallSentenceTaggedBail,
    remand = this.remand,
    taggedBail = this.taggedBail,
    unusedRemand = this.unusedRemand,
  )
}
