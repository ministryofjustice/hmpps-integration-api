package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApi
import java.time.LocalDate
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Sentence as IntegrationApiSentence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.SentenceLength as IntegrationApiSentenceLength
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis.Term as NomisTerm
data class Sentence(
  val fineAmount: Number? = null,
  val sentenceDate: LocalDate? = null,
  val sentenceStatus: String? = null,
  val sentenceTypeDescription: String? = null,
  val terms: List<NomisTerm> = listOf(NomisTerm()),
) {
  fun toSentence(): IntegrationApiSentence = IntegrationApiSentence(
    dataSource = UpstreamApi.NOMIS,
    dateOfSentencing = this.sentenceDate,
    description = this.sentenceTypeDescription,
    fineAmount = this.fineAmount,
    isActive = sentenceStatusToBoolean(this.sentenceStatus),
    isCustodial = true,
    length = IntegrationApiSentenceLength(terms = this.terms.map { it.toTerm() }),
  )
}
private fun sentenceStatusToBoolean(sentenceStatus: String?): Boolean? {
  return when (sentenceStatus) {
    "A" -> true
    "I" -> false
    else -> null
  }
}
