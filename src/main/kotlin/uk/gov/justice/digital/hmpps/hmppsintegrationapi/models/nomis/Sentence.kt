package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

import java.time.LocalDate
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Sentence as IntegrationApiSentence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis.Term as NomisTerm
data class Sentence(
  val sentenceDate: LocalDate? = null,
  val sentenceStatus: String? = null,
  val terms: List<NomisTerm> = listOf(NomisTerm()),
) {
  fun toSentence(): IntegrationApiSentence = IntegrationApiSentence(
    dateOfSentencing = this.sentenceDate,
    isActive = sentenceStatusToBoolean(this.sentenceStatus),
    terms = this.terms.map { it.toTerm() },
  )
}
private fun sentenceStatusToBoolean(sentenceStatus: String?): Boolean? {
  return when (sentenceStatus) {
    "A" -> true
    "I" -> false
    else -> null
  }
}
