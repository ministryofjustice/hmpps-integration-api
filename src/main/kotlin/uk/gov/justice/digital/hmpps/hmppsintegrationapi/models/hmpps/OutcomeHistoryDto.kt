package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class OutcomeHistoryDto(
  val hearing: HearingDto? = null,
  val outcome: CombinedOutcomeDto? = null,
)
