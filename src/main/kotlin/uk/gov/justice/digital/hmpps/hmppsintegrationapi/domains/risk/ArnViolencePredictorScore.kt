package uk.gov.justice.digital.hmpps.hmppsintegrationapi.domains.risk

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ViolencePredictor

data class ArnViolencePredictorScore(
  val ovpRisk: String? = null,
) {
  fun toViolencePredictor(): ViolencePredictor =
    ViolencePredictor(
      scoreLevel = this.ovpRisk,
    )
}
