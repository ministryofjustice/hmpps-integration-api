package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class OtherRisks(
  val escapeOrAbscond: String? = null,
  val controlIssuesDisruptiveBehaviour: String? = null,
  val breachOfTrust: String? = null,
  val riskToOtherPrisoners: String? = null,
)
