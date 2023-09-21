package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.SexualPredictor as IntegrationAPISexualPredictor

data class SexualPredictorScore(
  val ospIndecentScoreLevel: String? = null,
  val ospContactScoreLevel: String? = null,
) {
  fun toSexualPredictor(): IntegrationAPISexualPredictor = IntegrationAPISexualPredictor(
    indecentScoreLevel = this.ospIndecentScoreLevel,
    contactScoreLevel = this.ospContactScoreLevel,
  )
}
