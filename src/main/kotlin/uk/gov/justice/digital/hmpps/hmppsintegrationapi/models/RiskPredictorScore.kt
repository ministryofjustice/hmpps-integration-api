package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models

data class RiskPredictorScore(
  val generalPredictorScore: GeneralPredictorScore = GeneralPredictorScore(),
)
