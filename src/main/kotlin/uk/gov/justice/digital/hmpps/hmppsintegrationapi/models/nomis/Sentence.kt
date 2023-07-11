package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

import java.time.LocalDate
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Sentence as IntegrationApiSentence

data class Sentence(
  val sentenceDetail: SentenceDetail?,
) {
  fun toSentence(): IntegrationApiSentence = IntegrationApiSentence(
    startDate = this.sentenceDetail?.sentenceStartDate,
  )

  data class SentenceDetail(
    val sentenceStartDate: LocalDate?,
  )
}
