package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class RiskCategory(
  val offenderNo: String? = null,
  val assessments: List<RiskAssessment?> = emptyList(),
)
