package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.RiskSummary as IntegrationApiRiskSummary

data class RiskSummary(
  val whoIsAtRisk: String? = null,
  val natureOfRisk: String? = null,
  val riskImminence: String? = null,
  val riskIncreaseFactors: String? = null,
  val riskMitigationFactors: String? = null,
  val overallRiskLevel: String? = null,
  val riskInCommunity: Map<String, List<String>>? = null,
  val riskInCustody: Map<String, List<String>>? = null,
) {
  fun toRiskSummary(): IntegrationApiRiskSummary = IntegrationApiRiskSummary(
    whoIsAtRisk = this.whoIsAtRisk,
    natureOfRisk = this.natureOfRisk,
    riskImminence = this.riskImminence,
    riskIncreaseFactors = this.riskIncreaseFactors,
    riskMitigationFactors = this.riskMitigationFactors,
    overallRiskLevel = this.overallRiskLevel,
    riskInCommunity = if (!this.riskInCommunity.isNullOrEmpty()) toRiskInContext(this.riskInCommunity) else null,
    riskInCustody = if (!this.riskInCustody.isNullOrEmpty()) toRiskInContext(this.riskInCustody) else null,
  )

  private fun toRiskInContext(arnRiskInContext: Map<String, List<String>>): Map<String, String> {
    var riskInContext = mutableMapOf<String, String>()

    for ((riskLevel, personGroups) in arnRiskInContext) {
      for (personGroup in personGroups) {
        val personGroupKey = toCamelCase(personGroup)
        riskInContext[personGroupKey] = riskLevel
      }
    }
    return riskInContext
  }

  private fun toCamelCase(personGroup: String) =
    personGroup.split(" ").map { it.replaceFirstChar(Char::uppercaseChar) }.joinToString("")
      .replaceFirstChar(Char::lowercaseChar)
}
