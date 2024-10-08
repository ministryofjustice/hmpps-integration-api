package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.OtherRisks

data class ArnOtherRisks(
  val escapeOrAbscond: String? = null,
  val controlIssuesDisruptiveBehaviour: String? = null,
  val breachOfTrust: String? = null,
  val riskToOtherPrisoners: String? = null,
) {
  fun toOtherRisks(): OtherRisks =
    OtherRisks(
      escapeOrAbscond = this.escapeOrAbscond,
      controlIssuesDisruptiveBehaviour = this.controlIssuesDisruptiveBehaviour,
      breachOfTrust = this.breachOfTrust,
      riskToOtherPrisoners = this.riskToOtherPrisoners,
    )
}
