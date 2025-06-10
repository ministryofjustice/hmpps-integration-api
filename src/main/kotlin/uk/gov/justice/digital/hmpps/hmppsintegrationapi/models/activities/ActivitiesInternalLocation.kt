package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities

data class ActivitiesInternalLocation(
  val id: Int,
  val code: String,
  val description: String,
  val dpsLocationId: String?,
)
