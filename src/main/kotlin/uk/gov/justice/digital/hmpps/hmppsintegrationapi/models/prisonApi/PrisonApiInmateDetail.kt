package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.RiskCategory

data class PrisonApiInmateDetail(
  val offenderNo: String? = null,
  val assessments: List<PrisonApiAssessment> = emptyList(),
  val category: String? = null,
  val categoryCode: String? = null,
) {
  fun toRiskCategory(): RiskCategory =
    RiskCategory(
      offenderNo = this.offenderNo,
      assessments = this.assessments.map { it.toRiskAssessment() },
      category = this.category,
      categoryCode = this.categoryCode,
    )
}
