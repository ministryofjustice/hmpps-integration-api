package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.RiskPredictorScore
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.RiskScoreV2
import java.math.BigDecimal
import java.time.LocalDateTime

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
  val score: BigDecimal? = null,
)

data class ArnRiskPredictorScore(
  val completedDate: String? = null,
  val status: String? = null,
  val source: String? = null,
  val outputVersion: String,
  val output: ArnOutput,
) {
  fun toRiskPredictorScore(sendDecimals: Boolean = false): RiskPredictorScore {
    fun roundDownIfRequired(score: BigDecimal?): BigDecimal? = if (sendDecimals) score else score?.let { BigDecimal(it.toInt()) }

    when (val assessmentVersion = outputVersion.toInt()) {
      1 -> {
        return RiskPredictorScore(
          completedDate = getDateFromString(this.completedDate),
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
          completedDate = getDateFromString(this.completedDate),
          assessmentStatus = this.status,
          assessmentVersion = assessmentVersion,
          allReoffendingPredictor =
            RiskScoreV2(
              band = this.output.allReoffendingPredictor.band,
              score = roundDownIfRequired(this.output.allReoffendingPredictor.score),
            ),
          violentReoffendingPredictor =
            RiskScoreV2(
              band = this.output.violentReoffendingPredictor.band,
              score = roundDownIfRequired(this.output.violentReoffendingPredictor.score),
            ),
          seriousViolentReoffendingPredictor =
            RiskScoreV2(
              band = this.output.seriousViolentReoffendingPredictor.band,
              score = roundDownIfRequired(this.output.seriousViolentReoffendingPredictor.score),
            ),
          directContactSexualReoffendingPredictor =
            RiskScoreV2(
              band = this.output.directContactSexualReoffendingPredictor.band,
              score = roundDownIfRequired(this.output.directContactSexualReoffendingPredictor.score),
            ),
          indirectImageContactSexualReoffendingPredictor =
            RiskScoreV2(
              band = this.output.indirectImageContactSexualReoffendingPredictor.band,
              score = roundDownIfRequired(this.output.indirectImageContactSexualReoffendingPredictor.score),
            ),
          combinedSeriousReoffendingPredictor =
            RiskScoreV2(
              band = this.output.combinedSeriousReoffendingPredictor.band,
              score = roundDownIfRequired(this.output.combinedSeriousReoffendingPredictor.score),
            ),
        )
      }
      else -> {
        throw RuntimeException("Version not supported: $outputVersion")
      }
    }
  }
}

private fun getDateFromString(date: String?): LocalDateTime? {
  if (!date.isNullOrEmpty()) {
    return LocalDateTime.parse(date)
  }
  return null
}
