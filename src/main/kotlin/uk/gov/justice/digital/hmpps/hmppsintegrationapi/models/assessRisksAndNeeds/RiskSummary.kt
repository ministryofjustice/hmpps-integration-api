package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.RiskSummary as IntegrationApiRiskSummary

data class RiskSummary(
  val whoIsAtRisk: String? = null,
  val natureOfRisk: String? = null,
  val riskImminence: String? = null,
  val riskIncreaseFactors: String? = null,
  val riskMitigationFactors: String? = null,
  val overallRiskLevel: String? = null,
) {
  fun toRiskSummary(): IntegrationApiRiskSummary = IntegrationApiRiskSummary(
    whoIsAtRisk = this.whoIsAtRisk,
    natureOfRisk = this.natureOfRisk,
    riskImminence = this.riskImminence,
    riskIncreaseFactors = this.riskIncreaseFactors,
    riskMitigationFactors = this.riskMitigationFactors,
    overallRiskLevel = this.overallRiskLevel,
  )
}
