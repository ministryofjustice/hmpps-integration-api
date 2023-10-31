package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.SentenceAdjustment

data class SentenceAdjustment(
  val additionalDaysAwarded: Number? = null,
) {
  fun toSentenceAdjustment(): SentenceAdjustment = SentenceAdjustment(
    additionalDaysAwarded = this.additionalDaysAwarded,
  )
}
