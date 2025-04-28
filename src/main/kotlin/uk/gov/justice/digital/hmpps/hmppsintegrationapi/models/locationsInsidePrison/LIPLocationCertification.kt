package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.locationsInsidePrison

data class LIPLocationCertification(
  val certified: Boolean,
  val capacityOfCertifiedCell: Int,
)
