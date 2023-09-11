package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models

data class RiskPredictor(
  val generalPredictorScore: GeneralPredictorScore = GeneralPredictorScore(),
)
