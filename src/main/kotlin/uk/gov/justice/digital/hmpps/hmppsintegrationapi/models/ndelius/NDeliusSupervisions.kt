package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius

data class NDeliusSupervisions(
  val communityManager: NDeliusCommunityManager? = null,
  val mappaDetail: NDeliusMappaDetail? = null,
  val supervisions: List<NDeliusSupervision>,
)
