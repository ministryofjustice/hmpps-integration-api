package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.SentenceLength

data class NDeliusSentence(
  val date: String? = null,
  val description: String? = null,
  val length: Int? = null,
  val lengthUnits: String? = null,
) {
  fun toLength(): SentenceLength {
    return SentenceLength(
      duration = this.length,
      units = this.lengthUnits,
      terms = emptyList(),
    )
  }
}
