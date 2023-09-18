package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.RiskPredictorScore as IntegrationAPIRiskPredictorScore
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds.GeneralPredictorScore as ArnGeneralPredictorScore
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds.GroupReconvictionScore as ArnGroupReconvictionScore
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds.ViolencePredictorScore as ArnViolencePredictorScore

data class RiskPredictorScore(
  val generalPredictorScore: ArnGeneralPredictorScore = ArnGeneralPredictorScore(),
  val violencePredictorScore: ArnViolencePredictorScore = ArnViolencePredictorScore(),
  val groupReconvictionScore: ArnGroupReconvictionScore = ArnGroupReconvictionScore(),
) {
  fun toRiskPredictorScore(): IntegrationAPIRiskPredictorScore = IntegrationAPIRiskPredictorScore(
    generalPredictor = this.generalPredictorScore.toGeneralPredictor(),
    violencePredictor = this.violencePredictorScore.toViolencePredictor(),
    groupReconviction = this.groupReconvictionScore.toGroupReconviction(),
  )
}
