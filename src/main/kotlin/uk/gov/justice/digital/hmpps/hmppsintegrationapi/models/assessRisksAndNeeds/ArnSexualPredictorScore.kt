package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.SexualPredictor as HmppsSexualPredictor

data class ArnSexualPredictorScore(
  val ospIndecentScoreLevel: String? = null,
  val ospContactScoreLevel: String? = null,
) {
  fun toSexualPredictor(): HmppsSexualPredictor = HmppsSexualPredictor(
    indecentScoreLevel = this.ospIndecentScoreLevel,
    contactScoreLevel = this.ospContactScoreLevel,
  )
}
