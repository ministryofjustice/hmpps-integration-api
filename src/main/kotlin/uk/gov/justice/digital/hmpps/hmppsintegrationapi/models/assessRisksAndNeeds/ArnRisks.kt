package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import java.time.LocalDateTime
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Risks as IntegrationApiRisks

data class ArnRisks(
  val assessedOn: LocalDateTime? = null,
  val riskToSelf: ArnRiskToSelf = ArnRiskToSelf(),
  val otherRisks: ArnOtherRisks = ArnOtherRisks(),
  val summary: ArnRiskSummary = ArnRiskSummary(),
) {
  fun toRisks(): IntegrationApiRisks = IntegrationApiRisks(
    assessedOn = this.assessedOn,
    riskToSelf = this.riskToSelf.toRiskToSelf(),
    otherRisks = this.otherRisks.toOtherRisks(),
    summary = this.summary.toRiskSummary(),
  )
}
