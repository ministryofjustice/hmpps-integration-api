package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.locationsInsidePrison

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.LocationUsageItem

data class LIPLocationUsageItem(
  val usageType: String,
  val capacity: Int?,
  val sequence: Int,
) {
  fun toLocationUsageItem(): LocationUsageItem =
    LocationUsageItem(
      usageType = this.usageType,
      capacity = this.capacity,
      sequence = this.sequence,
    )
}
