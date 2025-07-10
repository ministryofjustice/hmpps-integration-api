package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.SuitabilityCriteria

data class ActivitiesSuitabilityCriteria(
  val riskLevel: String,
  val isPaid: Boolean,
  val payRate: List<ActivitiesPayRate>,
  val minimumEducationLevel: List<ActivitiesMinimumEducationLevel>,
) {
  fun toSuitabilityCriteria() =
    SuitabilityCriteria(
      riskLevel = this.riskLevel,
      isPaid = this.isPaid,
      payRate = this.payRate.map { it.toPayRate() },
      minimumEducationLevel = this.minimumEducationLevel.map { it.toMinimumEducationLevel() },
    )
}
