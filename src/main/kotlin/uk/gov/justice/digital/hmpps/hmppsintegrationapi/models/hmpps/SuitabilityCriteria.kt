package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class SuitabilityCriteria(
  val riskLevel: String,
  val isPaid: Boolean,
  val payRate: PayRate,
  val minimumEducationLevel: List<MinimumEducationLevel>,
)
