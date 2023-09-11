package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds.RiskPredictor as ArnRiskPredictor

data class RiskPredictors(
  val arnRiskPredictors: List<ArnRiskPredictor>,
)
