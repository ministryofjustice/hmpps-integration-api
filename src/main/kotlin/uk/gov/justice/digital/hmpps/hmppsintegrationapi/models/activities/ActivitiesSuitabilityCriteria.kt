package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities

data class ActivitiesSuitabilityCriteria(
  val riskLevel: String,
  val isPaid: Boolean,
  val payRate: ActivitiesPayRate,
  val minimumEducationLevel: List<ActivitiesMinimumEducationLevel>,
)
