package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.locationsInsidePrison

data class LIPUsageItem(
  val usageType: String,
  val capacity: Int?,
  val sequence: Int
)
