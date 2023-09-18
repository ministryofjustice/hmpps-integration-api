package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.RiskPredictorScore as IntegrationAPIRiskPredictorScore
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds.GeneralPredictorScore as ArnGeneralPredictorScore
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds.ViolencePredictorScore as ArnViolencePredictorScore

data class RiskPredictorScore(
  val generalPredictorScore: ArnGeneralPredictorScore = ArnGeneralPredictorScore(),
  val violencePredictorScore: ArnViolencePredictorScore = ArnViolencePredictorScore(),
) {
  fun toRiskPredictorScore(): IntegrationAPIRiskPredictorScore = IntegrationAPIRiskPredictorScore(
    generalPredictor = this.generalPredictorScore.toGeneralPredictor(),
    violencePredictor = this.violencePredictorScore.toViolencePredictor(),
  )
}
