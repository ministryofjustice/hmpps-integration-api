package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

import java.time.LocalDate
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Sentence as IntegrationApiSentence

data class Sentence(
  val sentenceDate: LocalDate?,
) {
  fun toSentence(): IntegrationApiSentence = IntegrationApiSentence(
    dateOfSentencing = this.sentenceDate,
  )
}
