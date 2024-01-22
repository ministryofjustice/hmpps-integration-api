package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class HearingDto(
  val dateTimeOfHearing: String? = null,
  val oicHearingType: String? = null,
  val outcome: HearingOutcomeDto? = null,
)
