package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.RiskPredictor as IntegrationAPIRiskPredictor
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds.GeneralPredictorScore as ArnGeneralPredictorScore

data class RiskPredictor(
  val generalPredictorScore: ArnGeneralPredictorScore = ArnGeneralPredictorScore(),
) {
  fun toRiskPredictor(): IntegrationAPIRiskPredictor = IntegrationAPIRiskPredictor(
    generalPredictorScore = this.generalPredictorScore.toGeneralPredictorScore(),
  )
}
