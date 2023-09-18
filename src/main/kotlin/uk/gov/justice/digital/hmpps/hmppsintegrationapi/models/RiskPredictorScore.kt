package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models

data class RiskPredictorScore(
  val generalPredictor: GeneralPredictor = GeneralPredictor(),
  val violencePredictorScore: ViolencePredictorScore = ViolencePredictorScore(),
)
