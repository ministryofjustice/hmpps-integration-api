package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.locationsInsidePrison

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.LocationCapacity

data class LIPLocationCapacity(
  val maxCapacity: Int,
  val workingCapacity: Int,
) {
  fun toLocationCapacity(): LocationCapacity =
    LocationCapacity(
      maxCapacity = maxCapacity,
      workingCapacity = workingCapacity,
    )
}
