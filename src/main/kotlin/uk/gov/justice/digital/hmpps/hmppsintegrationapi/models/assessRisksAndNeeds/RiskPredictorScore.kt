package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.RiskPredictorScore as IntegrationAPIRiskPredictorScore
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds.GeneralPredictorScore as ArnGeneralPredictorScore

data class RiskPredictorScore(
  val generalPredictorScore: ArnGeneralPredictorScore = ArnGeneralPredictorScore(),
) {
  fun toRiskPredictorScore(): IntegrationAPIRiskPredictorScore = IntegrationAPIRiskPredictorScore(
    generalPredictorScore = this.generalPredictorScore.toGeneralPredictorScore(),
  )
}
