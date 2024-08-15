package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class SentenceAdjustment(
  @Schema(description = "Number of additional days awarded", example = "10")
  val additionalDaysAwarded: Number? = null,
  @Schema(description = "Number unlawfully at large days", example = "16")
  val unlawfullyAtLarge: Number? = null,
  @Schema(description = "Number of lawfully at large days", example = "11")
  val lawfullyAtLarge: Number? = null,
  @Schema(description = "Number of restored additional days awarded", example = "20")
  val restoredAdditionalDaysAwarded: Number? = null,
  @Schema(description = "Number of special remission days", example = "14")
  val specialRemission: Number? = null,
  @Schema(description = "Number of recall sentence remand days", example = "7")
  val recallSentenceRemand: Number? = null,
  @Schema(description = "Number of recall sentence tagged bail days", example = "19")
  val recallSentenceTaggedBail: Number? = null,
  @Schema(description = "Number of remand days", example = "3")
  val remand: Number? = null,
  @Schema(description = "Number of tagged bail days", example = "13")
  val taggedBail: Number? = null,
  @Schema(description = "Number of unused remand days", example = "13")
  val unusedRemand: Number? = null,
)
