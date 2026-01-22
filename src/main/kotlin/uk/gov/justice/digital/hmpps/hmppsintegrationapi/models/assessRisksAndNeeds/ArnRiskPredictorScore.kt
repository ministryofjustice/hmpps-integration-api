package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.RiskPredictorScore
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.RiskScoreV2
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
  // Version 1
  val generalPredictorScore: ArnGeneralPredictorScore = ArnGeneralPredictorScore(),
  val violencePredictorScore: ArnViolencePredictorScore = ArnViolencePredictorScore(),
  val groupReconvictionScore: ArnGroupReconvictionScore = ArnGroupReconvictionScore(),
  val riskOfSeriousRecidivismScore: ArnRiskOfSeriousRecidivismScore = ArnRiskOfSeriousRecidivismScore(),
  val sexualPredictorScore: ArnSexualPredictorScore = ArnSexualPredictorScore(),
  // Version 2
  val allReoffendingPredictor: ArnScore = ArnScore(),
  val violentReoffendingPredictor: ArnScore = ArnScore(),
  val seriousViolentReoffendingPredictor: ArnScore = ArnScore(),
  val directContactSexualReoffendingPredictor: ArnScore = ArnScore(),
  val indirectImageContactSexualReoffendingPredictor: ArnScore = ArnScore(),
  val combinedSeriousReoffendingPredictor: ArnScore = ArnScore(),
)

data class ArnScore(
  val band: String? = null,
)

data class ArnRiskPredictorScoreV2(
  val completedDate: String? = null,
  val status: String? = null,
  val source: String? = null,
  val outputVersion: String,
  val output: ArnOutput,
) {
  fun toRiskPredictorScore(): RiskPredictorScore {
    when (val assessmentVersion = outputVersion.toInt()) {
      1 -> {
        return RiskPredictorScore(
          completedDate = if (!this.completedDate.isNullOrEmpty()) LocalDateTime.parse(this.completedDate) else null,
          assessmentStatus = this.status,
          generalPredictor = this.output.generalPredictorScore.toGeneralPredictor(),
          violencePredictor = this.output.violencePredictorScore.toViolencePredictor(),
          groupReconviction = this.output.groupReconvictionScore.toGroupReconviction(),
          riskOfSeriousRecidivism = this.output.riskOfSeriousRecidivismScore.toRiskOfSeriousRecidivism(),
          sexualPredictor = this.output.sexualPredictorScore.toSexualPredictor(),
          assessmentVersion = assessmentVersion,
        )
      }
      2 -> {
        return RiskPredictorScore(
          completedDate = if (!this.completedDate.isNullOrEmpty()) LocalDateTime.parse(this.completedDate) else null,
          assessmentStatus = this.status,
          assessmentVersion = assessmentVersion,
          allReoffendingPredictor = RiskScoreV2(band = this.output.allReoffendingPredictor.band),
          violentReoffendingPredictor = RiskScoreV2(band = this.output.violentReoffendingPredictor.band),
          seriousViolentReoffendingPredictor = RiskScoreV2(band = this.output.seriousViolentReoffendingPredictor.band),
          directContactSexualReoffendingPredictor = RiskScoreV2(band = this.output.directContactSexualReoffendingPredictor.band),
          indirectImageContactSexualReoffendingPredictor = RiskScoreV2(band = this.output.indirectImageContactSexualReoffendingPredictor.band),
          combinedSeriousReoffendingPredictor = RiskScoreV2(band = this.output.combinedSeriousReoffendingPredictor.band),
        )
      }
      else -> {
        throw RuntimeException("Version not supported: $outputVersion")
      }
    }
  }
}
