package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.RiskCategory

data class NomisInmateDetail(
  val offenderNo: String? = null,
  val assessments: List<NomisAssessment> = emptyList(),
) {
  fun toRiskCategory(): RiskCategory = RiskCategory(
    offenderNo = this.offenderNo,
    assessments = this.assessments.map { it.toRiskAssessment() },
  )
}
