package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.RiskPredictorScore
import java.time.LocalDateTime

data class ArnRiskPredictorScore(
  val completedDate: String? = null,
  val assessmentStatus: String? = null,
  val generalPredictorScore: ArnGeneralPredictorScore = ArnGeneralPredictorScore(),
  val violencePredictorScore: ArnViolencePredictorScore = ArnViolencePredictorScore(),
  val groupReconvictionScore: ArnGroupReconvictionScore = ArnGroupReconvictionScore(),
  val riskOfSeriousRecidivismScore: ArnRiskOfSeriousRecidivismScore = ArnRiskOfSeriousRecidivismScore(),
  val sexualPredictorScore: ArnSexualPredictorScore = ArnSexualPredictorScore(),
) {
  fun toRiskPredictorScore(): RiskPredictorScore =
    RiskPredictorScore(
      completedDate = if (!this.completedDate.isNullOrEmpty()) LocalDateTime.parse(this.completedDate) else null,
      assessmentStatus = this.assessmentStatus,
      generalPredictor = this.generalPredictorScore.toGeneralPredictor(),
      violencePredictor = this.violencePredictorScore.toViolencePredictor(),
      groupReconviction = this.groupReconvictionScore.toGroupReconviction(),
      riskOfSeriousRecidivism = this.riskOfSeriousRecidivismScore.toRiskOfSeriousRecidivism(),
      sexualPredictor = this.sexualPredictorScore.toSexualPredictor(),
    )
}
