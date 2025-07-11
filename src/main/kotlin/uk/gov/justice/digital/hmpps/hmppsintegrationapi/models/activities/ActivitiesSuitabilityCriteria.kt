package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.SuitabilityCriteria

data class ActivitiesSuitabilityCriteria(
  val riskLevel: String,
  val isPaid: Boolean,
  val payRates: List<ActivitiesPayRate>,
  val minimumEducationLevel: List<ActivitiesMinimumEducationLevel>,
) {
  fun toSuitabilityCriteria() =
    SuitabilityCriteria(
      riskLevel = this.riskLevel,
      isPaid = this.isPaid,
      payRates = this.payRates.map { it.toPayRate() },
      minimumEducationLevel = this.minimumEducationLevel.map { it.toMinimumEducationLevel() },
    )
}
