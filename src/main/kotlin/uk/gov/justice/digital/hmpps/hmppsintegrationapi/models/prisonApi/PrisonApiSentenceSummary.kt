package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi

data class PrisonApiSentenceSummary(
  val latestPrisonTerm: PrisonApiPrisonTerm = PrisonApiPrisonTerm(),
)
