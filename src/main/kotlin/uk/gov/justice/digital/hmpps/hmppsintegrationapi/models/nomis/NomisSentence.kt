package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Sentence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.SentenceLength
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.SystemSource
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import java.time.LocalDate

data class NomisSentence(
  val fineAmount: Number? = null,
  val sentenceDate: LocalDate? = null,
  val sentenceStatus: String? = null,
  val sentenceTypeDescription: String? = null,
  val terms: List<NomisTerm> = listOf(NomisTerm()),
) {
  fun toSentence(): Sentence =
    Sentence(
      serviceSource = UpstreamApi.NOMIS,
      systemSource = SystemSource.PRISON_SYSTEMS,
      dateOfSentencing = this.sentenceDate,
      description = this.sentenceTypeDescription,
      fineAmount = this.fineAmount,
      isActive = sentenceStatusToBoolean(this.sentenceStatus),
      isCustodial = true,
      length = SentenceLength(terms = this.terms.map { it.toTerm() }),
    )
}

private fun sentenceStatusToBoolean(sentenceStatus: String?): Boolean? =
  when (sentenceStatus) {
    "A" -> true
    "I" -> false
    else -> null
  }
