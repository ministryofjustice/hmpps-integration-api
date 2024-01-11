package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import java.time.LocalDateTime

data class Risks(
  val assessedOn: LocalDateTime? = null,
  val riskToSelf: RiskToSelf = RiskToSelf(),
  val otherRisks: OtherRisks = OtherRisks(),
  val summary: RiskSummary = RiskSummary(),
)
