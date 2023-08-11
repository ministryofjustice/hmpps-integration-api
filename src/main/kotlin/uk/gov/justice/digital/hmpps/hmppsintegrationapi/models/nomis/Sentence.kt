package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

import java.time.LocalDate
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Sentence as IntegrationApiSentence

data class Sentence(
  val sentenceDate: LocalDate?,
  val sentenceStatus: String?,
) {
  fun toSentence(): IntegrationApiSentence = IntegrationApiSentence(
    dateOfSentencing = this.sentenceDate,
    isActive = toIsActive(this.sentenceStatus),
  )
}
private fun toIsActive(sentenceStatus: String?): Boolean? {
  return when (sentenceStatus) {
    "A" -> true
    "I" -> false
    else -> null
  }
}
