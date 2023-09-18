package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ViolencePredictorScore as IntegrationAPIViolencePredictorScore

data class ViolencePredictorScore(
  val ovpRisk: String? = null,
) {
  fun toViolencePredictorScore(): IntegrationAPIViolencePredictorScore = IntegrationAPIViolencePredictorScore(
    ovpRisk = this.ovpRisk,
  )
}
