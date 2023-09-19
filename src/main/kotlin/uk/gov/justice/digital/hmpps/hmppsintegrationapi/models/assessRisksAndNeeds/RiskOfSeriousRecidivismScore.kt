package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.RiskOfSeriousRecidivism as IntegrationAPIRiskOfSeriousRecidivism

data class RiskOfSeriousRecidivismScore(
  val scoreLevel: String? = null,
) {
  fun toRiskOfSeriousRecidivism(): IntegrationAPIRiskOfSeriousRecidivism = IntegrationAPIRiskOfSeriousRecidivism(
    scoreLevel = this.scoreLevel,
  )
}
