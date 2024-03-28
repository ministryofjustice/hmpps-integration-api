package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Risks
import java.time.LocalDateTime

data class ArnRisks(
  val assessedOn: LocalDateTime? = null,
  val riskToSelf: ArnRiskToSelf = ArnRiskToSelf(),
  val otherRisks: ArnOtherRisks = ArnOtherRisks(),
  val summary: ArnRiskSummary = ArnRiskSummary(),
) {
  fun toRisks(): Risks =
    Risks(
      assessedOn = this.assessedOn,
      riskToSelf = this.riskToSelf.toRiskToSelf(),
      otherRisks = this.otherRisks.toOtherRisks(),
      summary = this.summary.toRiskSummary(),
    )
}
