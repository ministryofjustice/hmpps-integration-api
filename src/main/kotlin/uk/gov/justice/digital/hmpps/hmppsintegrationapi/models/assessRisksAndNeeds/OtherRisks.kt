package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.OtherRisks as IntegrationApiOtherRisks

data class OtherRisks(
  val escapeOrAbscond: String? = null,
  val controlIssuesDisruptiveBehaviour: String? = null,
  val breachOfTrust: String? = null,
  val riskToOtherPrisoners: String? = null,
) {
  fun toOtherRisks(): IntegrationApiOtherRisks = IntegrationApiOtherRisks(
    escapeOrAbscond = this.escapeOrAbscond,
    controlIssuesDisruptiveBehaviour = this.controlIssuesDisruptiveBehaviour,
    breachOfTrust = this.breachOfTrust,
    riskToOtherPrisoners = this.riskToOtherPrisoners,
  )
}
