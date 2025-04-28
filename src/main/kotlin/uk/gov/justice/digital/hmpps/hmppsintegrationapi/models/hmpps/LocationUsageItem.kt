package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class LocationUsageItem(
  val usageType: String,
  val capacity: Int?,
  val sequence: Int,
)
