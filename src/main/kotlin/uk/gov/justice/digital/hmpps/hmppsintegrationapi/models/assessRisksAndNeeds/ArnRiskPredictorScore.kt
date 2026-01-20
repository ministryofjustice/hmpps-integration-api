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

data class ArnOutput(
  val generalPredictorScore: ArnGeneralPredictorScore = ArnGeneralPredictorScore(),
  val violencePredictorScore: ArnViolencePredictorScore = ArnViolencePredictorScore(),
  val groupReconvictionScore: ArnGroupReconvictionScore = ArnGroupReconvictionScore(),
  val riskOfSeriousRecidivismScore: ArnRiskOfSeriousRecidivismScore = ArnRiskOfSeriousRecidivismScore(),
  val sexualPredictorScore: ArnSexualPredictorScore = ArnSexualPredictorScore(),
)

data class ArnRiskPredictorScoreV2(
  val completedDate: String? = null,
  val status: String? = null,
  val source: String? = null,
  val outputVersion: String? = null,
  val output: ArnOutput,
) {
  fun toRiskPredictorScore(): RiskPredictorScore =
    RiskPredictorScore(
      completedDate = if (!this.completedDate.isNullOrEmpty()) LocalDateTime.parse(this.completedDate) else null,
      assessmentStatus = this.status,
      generalPredictor = this.output.generalPredictorScore.toGeneralPredictor(),
      violencePredictor = this.output.violencePredictorScore.toViolencePredictor(),
      groupReconviction = this.output.groupReconvictionScore.toGroupReconviction(),
      riskOfSeriousRecidivism = this.output.riskOfSeriousRecidivismScore.toRiskOfSeriousRecidivism(),
      sexualPredictor = this.output.sexualPredictorScore.toSexualPredictor(),
    )
}
