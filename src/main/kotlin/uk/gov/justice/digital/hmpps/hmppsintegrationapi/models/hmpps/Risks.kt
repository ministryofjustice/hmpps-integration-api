package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

data class Risks(
  @Schema(description = "Date of risk assessment", example = "2023-09-05T10:15:41")
  val assessedOn: LocalDateTime? = null,
  val riskToSelf: RiskToSelf = RiskToSelf(),
  val otherRisks: OtherRisks = OtherRisks(),
  val summary: RiskSummary = RiskSummary(),
)
