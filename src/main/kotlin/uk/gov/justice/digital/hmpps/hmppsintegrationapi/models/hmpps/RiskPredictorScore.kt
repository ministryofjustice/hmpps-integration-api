package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

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
  val generalPredictor: GeneralPredictor = GeneralPredictor(),
  val violencePredictor: ViolencePredictor = ViolencePredictor(),
  val groupReconviction: GroupReconviction = GroupReconviction(),
  val riskOfSeriousRecidivism: RiskOfSeriousRecidivism = RiskOfSeriousRecidivism(),
  val sexualPredictor: SexualPredictor = SexualPredictor(),
)
