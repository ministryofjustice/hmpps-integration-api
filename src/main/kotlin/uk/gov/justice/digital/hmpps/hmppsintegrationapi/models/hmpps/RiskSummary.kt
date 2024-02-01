package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class RiskSummary(
  val whoIsAtRisk: String? = null,
  val natureOfRisk: String? = null,
  val riskImminence: String? = null,
  val riskIncreaseFactors: String? = null,
  val riskMitigationFactors: String? = null,
  val overallRiskLevel: String? = null,
  val riskInCommunity: Map<String, String>? = null,
  val riskInCustody: Map<String, String>? = null,
)
