package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ViolencePredictor as IntegrationAPIViolencePredictor

data class ViolencePredictorScore(
  val ovpRisk: String? = null,
) {
  fun toViolencePredictor(): IntegrationAPIViolencePredictor = IntegrationAPIViolencePredictor(
    scoreLevel = this.ovpRisk,
  )
}
