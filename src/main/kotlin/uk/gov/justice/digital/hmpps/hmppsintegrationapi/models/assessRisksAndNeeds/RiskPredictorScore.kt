package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import java.time.LocalDateTime
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.RiskPredictorScore as IntegrationAPIRiskPredictorScore
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds.GeneralPredictorScore as ArnGeneralPredictorScore
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds.GroupReconvictionScore as ArnGroupReconvictionScore
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds.ViolencePredictorScore as ArnViolencePredictorScore

data class RiskPredictorScore(
  val completedDate: String? = null,
  val assessmentStatus: String? = null,
  val generalPredictorScore: ArnGeneralPredictorScore = ArnGeneralPredictorScore(),
  val violencePredictorScore: ArnViolencePredictorScore = ArnViolencePredictorScore(),
  val groupReconvictionScore: ArnGroupReconvictionScore = ArnGroupReconvictionScore(),
) {
  fun toRiskPredictorScore(): IntegrationAPIRiskPredictorScore = IntegrationAPIRiskPredictorScore(
    completedDate = if (!this.completedDate.isNullOrEmpty()) LocalDateTime.parse(this.completedDate) else null,
    assessmentStatus = this.assessmentStatus,
    generalPredictor = this.generalPredictorScore.toGeneralPredictor(),
    violencePredictor = this.violencePredictorScore.toViolencePredictor(),
    groupReconviction = this.groupReconvictionScore.toGroupReconviction(),
  )
}
