package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

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
)
