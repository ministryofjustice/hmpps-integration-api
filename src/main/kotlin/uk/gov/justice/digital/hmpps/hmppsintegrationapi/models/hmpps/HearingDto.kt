package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class HearingDto(
  val id: Number? = null,
  val locationId: Number? = null,
  val dateTimeOfHearing: String? = null,
  val oicHearingType: String? = null,
  val outcome: HearingOutcomeDto? = null,
  val agencyId: String? = null,
)
