package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.RiskAssessment

data class NomisAssessment(
  val classificationCode: String? = null,
  val classification: String? = null,
  val assessmentCode: String? = null,
  val assessmentDescription: String? = null,
  val assessmentDate: String? = null,
  val nextReviewDate: String? = null,
  val assessmentAgencyId: String? = null,
  val assessmentStatus: String? = null,
  val assessmentComment: String? = null,
) {
  fun toRiskAssessment() = RiskAssessment(
    classificationCode = this.classificationCode,
    classification = this.classification,
    assessmentCode = this.assessmentCode,
    assessmentDescription = this.assessmentDescription,
    assessmentDate = this.assessmentDate,
    nextReviewDate = this.nextReviewDate,
    assessmentAgencyId = this.assessmentAgencyId,
    assessmentStatus = this.assessmentStatus,
    assessmentComment = this.assessmentComment,
  )
}
