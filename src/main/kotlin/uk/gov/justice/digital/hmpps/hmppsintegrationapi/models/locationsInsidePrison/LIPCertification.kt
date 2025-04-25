package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.locationsInsidePrison

data class LIPCertification(
  val certified: Boolean,
  val capacityOfCertifiedCell: Int,
)
