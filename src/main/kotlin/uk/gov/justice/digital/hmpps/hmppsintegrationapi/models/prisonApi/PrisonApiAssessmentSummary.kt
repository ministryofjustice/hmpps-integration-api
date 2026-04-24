package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi

data class PrisonApiAssessmentSummary(
  val bookingId: Long? = null,
  val assessmentSeq: Int? = null,
  val offenderNo: String? = null,
  val classificationCode: String? = null,
  val assessmentCode: String? = null,
  val cellSharingAlertFlag: Boolean? = null,
  val assessmentDate: String? = null,
  val assessmentAgencyId: String? = null,
  val assessmentComment: String? = null,
  val assessorUser: String? = null,
  val nextReviewDate: String? = null,
)
