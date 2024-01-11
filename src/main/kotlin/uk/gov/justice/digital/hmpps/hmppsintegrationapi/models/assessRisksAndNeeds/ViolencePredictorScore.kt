package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ViolencePredictor as HmppsViolencePredictor

data class ViolencePredictorScore(
  val ovpRisk: String? = null,
) {
  fun toViolencePredictor(): HmppsViolencePredictor = HmppsViolencePredictor(
    scoreLevel = this.ovpRisk,
  )
}
