package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.RiskPredictors as IntegrationAPIRiskPredictors
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds.GeneralPredictorScore as ArnGeneralPredictorScore

data class RiskPredictors(
  val generalPredictorScore: ArnGeneralPredictorScore = ArnGeneralPredictorScore(),
) {
  fun toRiskPredictors() = IntegrationAPIRiskPredictors(
    generalPredictorScore = this.generalPredictorScore.toGeneralPredictorScore(),
  )
}
