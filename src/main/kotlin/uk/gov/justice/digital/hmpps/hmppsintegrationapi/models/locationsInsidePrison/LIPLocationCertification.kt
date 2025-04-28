package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.locationsInsidePrison

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.LocationCertification

data class LIPLocationCertification(
  val certified: Boolean,
  val capacityOfCertifiedCell: Int,
) {
  fun toLocationCertification(): LocationCertification =
    LocationCertification(
      certified = this.certified,
      capacityOfCertifiedCell = this.capacityOfCertifiedCell,
    )
}
