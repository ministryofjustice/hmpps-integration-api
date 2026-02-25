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

fun roundDownIfRequired(
  score: BigDecimal?,
  sendDecimals: Boolean,
): BigDecimal? = if (sendDecimals) score else score?.let { BigDecimal(it.toInt()) }

data class ArnRiskPredictorScore(
  val completedDate: String? = null,
  val status: String? = null,
  val source: String? = null,
  val outputVersion: String,
  val output: ArnOutput,
) {
  fun toRiskPredictorScore(sendDecimals: Boolean = false): RiskPredictorScore {
    when (val assessmentVersion = outputVersion.toInt()) {
      1 -> {
        return RiskPredictorScore(
          completedDate = getDateFromString(this.completedDate),
          assessmentStatus = this.status,
          generalPredictor = this.output.generalPredictorScore.toGeneralPredictor(sendDecimals = sendDecimals),
          violencePredictor = this.output.violencePredictorScore.toViolencePredictor(sendDecimals = sendDecimals),
          groupReconviction = this.output.groupReconvictionScore.toGroupReconviction(sendDecimals = sendDecimals),
          riskOfSeriousRecidivism = this.output.riskOfSeriousRecidivismScore.toRiskOfSeriousRecidivism(sendDecimals = sendDecimals),
          sexualPredictor = this.output.sexualPredictorScore.toSexualPredictor(sendDecimals = sendDecimals),
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
              score = roundDownIfRequired(this.output.allReoffendingPredictor.score, sendDecimals),
            ),
          violentReoffendingPredictor =
            RiskScoreV2(
              band = this.output.violentReoffendingPredictor.band,
              score = roundDownIfRequired(this.output.violentReoffendingPredictor.score, sendDecimals),
            ),
          seriousViolentReoffendingPredictor =
            RiskScoreV2(
              band = this.output.seriousViolentReoffendingPredictor.band,
              score = roundDownIfRequired(this.output.seriousViolentReoffendingPredictor.score, sendDecimals),
            ),
          directContactSexualReoffendingPredictor =
            RiskScoreV2(
              band = this.output.directContactSexualReoffendingPredictor.band,
              score = roundDownIfRequired(this.output.directContactSexualReoffendingPredictor.score, sendDecimals),
            ),
          indirectImageContactSexualReoffendingPredictor =
            RiskScoreV2(
              band = this.output.indirectImageContactSexualReoffendingPredictor.band,
              score = roundDownIfRequired(this.output.indirectImageContactSexualReoffendingPredictor.score, sendDecimals),
            ),
          combinedSeriousReoffendingPredictor =
            RiskScoreV2(
              band = this.output.combinedSeriousReoffendingPredictor.band,
              score = roundDownIfRequired(this.output.combinedSeriousReoffendingPredictor.score, sendDecimals),
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
