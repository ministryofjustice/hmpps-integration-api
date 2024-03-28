package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius

data class NDeliusSupervisions(
  val mappaDetail: NDeliusMappaDetail? = null,
  val supervisions: List<NDeliusSupervision>,
)
