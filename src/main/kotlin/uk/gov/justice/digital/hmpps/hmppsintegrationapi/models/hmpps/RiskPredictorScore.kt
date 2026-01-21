package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
data class RiskPredictorScore(
  @Schema(description = "Risk scores calculation completion date", example = "2023-09-05T10:15:41")
  val completedDate: LocalDateTime? = null,
  @Schema(
    description = """
      Whether the risk score calculation is complete. Possible values are:
      `COMPLETE`,
      `LOCKED_INCOMPLETE`
    """,
    example = "COMPLETED",
    allowableValues = ["COMPLETED", "LOCKED_INCOMPLETE"],
  )
  val assessmentStatus: String? = null,
  // Version 1
  val generalPredictor: GeneralPredictor = GeneralPredictor(),
  val violencePredictor: ViolencePredictor = ViolencePredictor(),
  val groupReconviction: GroupReconviction = GroupReconviction(),
  val riskOfSeriousRecidivism: RiskOfSeriousRecidivism = RiskOfSeriousRecidivism(),
  val sexualPredictor: SexualPredictor = SexualPredictor(),
  // Version 2
  val allReoffendingPredictor: RiskScoreV2? = null,
  val violentReoffendingPredictor: RiskScoreV2? = null,
  val seriousViolentReoffendingPredictor: RiskScoreV2? = null,
  val directContactSexualReoffendingPredictor: RiskScoreV2? = null,
  val indirectImageContactSexualReoffendingPredictor: RiskScoreV2? = null,
  val combinedSeriousReoffendingPredictor: RiskScoreV2? = null,
)
