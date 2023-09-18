package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models

data class RiskPredictorScore(
  val generalPredictor: GeneralPredictor = GeneralPredictor(),
  val violencePredictor: ViolencePredictor = ViolencePredictor(),
  val groupReconviction: GroupReconviction = GroupReconviction(),
)
