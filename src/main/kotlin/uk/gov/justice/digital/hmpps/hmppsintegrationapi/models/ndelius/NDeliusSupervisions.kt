package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius

data class NDeliusSupervisions(
  val communityManager: NDeliusCommunityManager,
  val mappaDetail: NDeliusMappaDetail? = null,
  val supervisions: List<NDeliusSupervision>,
  val dynamicRisks: List<NDeliusDynamicRisk>,
)
