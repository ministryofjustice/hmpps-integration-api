package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class RiskAssessment(
  val classificationCode: String? = null,
  val classification: String? = null,
  val assessmentCode: String? = null,
  val assessmentDescription: String? = null,
  val assessmentDate: String? = null,
  val nextReviewDate: String? = null,
  val assessmentAgencyId: String? = null,
  val assessmentStatus: String? = null,
  val assessmentComment: String? = null,
)
