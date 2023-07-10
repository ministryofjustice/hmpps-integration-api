package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Sentence
import java.time.LocalDate

data class Sentence(
  val sentenceStartDate: LocalDate? = null,
) {
  fun toSentence(): Sentence = Sentence(
    startDate = this.sentenceStartDate,
  )
}
