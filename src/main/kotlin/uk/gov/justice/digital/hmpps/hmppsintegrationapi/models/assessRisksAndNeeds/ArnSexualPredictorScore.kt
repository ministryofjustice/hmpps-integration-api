package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.SexualPredictor

data class ArnSexualPredictorScore(
  val ospIndecentScoreLevel: String? = null,
  val ospContactScoreLevel: String? = null,
) {
  fun toSexualPredictor(): SexualPredictor = SexualPredictor(
    indecentScoreLevel = this.ospIndecentScoreLevel,
    contactScoreLevel = this.ospContactScoreLevel,
  )
}
