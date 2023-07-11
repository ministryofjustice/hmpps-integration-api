package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Sentence as IntegrationApiSentence

data class Sentence(
  val sentenceDetail: SentenceDetail?,
) {
  fun toSentence() = IntegrationApiSentence(
    startDate = this.sentenceDetail?.sentenceStartDate,
  )
}
