package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import java.time.LocalDateTime
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Risks as IntegrationApiRisks

data class Risks(
  val assessedOn: LocalDateTime? = null,
  val riskToSelf: RiskToSelf = RiskToSelf(),
  val otherRisks: OtherRisks = OtherRisks(),
  val summary: RiskSummary = RiskSummary(),
) {
  fun toRisks(): IntegrationApiRisks = IntegrationApiRisks(
    assessedOn = this.assessedOn,
    riskToSelf = this.riskToSelf.toRiskToSelf(),
    otherRisks = this.otherRisks.toOtherRisks(),
    summary = this.summary.toRiskSummary(),
  )
}
