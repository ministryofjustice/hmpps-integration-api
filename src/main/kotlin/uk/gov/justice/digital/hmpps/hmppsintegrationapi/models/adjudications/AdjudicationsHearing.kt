package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.adjudications

data class AdjudicationsHearing(
  val id: Number? = null,
  val locationId: Number? = null,
  val dateTimeOfHearing: String? = null,
  val oicHearingType: String? = null,
  val outcome: AdjudicationHearingOutcome? = null,
  val agencyId: String? = null,
)
