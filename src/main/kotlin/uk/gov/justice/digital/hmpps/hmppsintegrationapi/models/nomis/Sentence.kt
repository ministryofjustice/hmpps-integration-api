package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.SentenceLength
import java.time.LocalDate
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Sentence as IntegrationApiSentence

data class Sentence(
  val sentenceDate: LocalDate?,
) {
  fun toSentence() = IntegrationApiSentence(
    dateOfSentencing = this.sentenceDate,
  )
}
