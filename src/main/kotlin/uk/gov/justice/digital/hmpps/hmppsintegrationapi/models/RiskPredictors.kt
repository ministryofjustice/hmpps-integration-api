package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models

data class RiskPredictors(
  val generalPredictorScore: GeneralPredictorScore = GeneralPredictorScore(),
)
