package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

data class OffenderSentence(
  val sentenceDetail: SentenceKeyDates = SentenceKeyDates(),
)
