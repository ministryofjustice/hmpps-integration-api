package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi

data class PrisonApiOffenderSentence(
  val sentenceDetail: PrisonApiSentenceKeyDates = PrisonApiSentenceKeyDates(),
)
