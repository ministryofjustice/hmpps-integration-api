package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.incentives

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.IEPLevel

data class IncIEPReviewHistory(
  val id: Int,
  val iepCode: String,
  val iepLevel: String,
  val prisonerNumber: String,
  val bookingId: Long,
  val iepDate: String,
  val iepTime: String,
  val iepDetails: List<IncIEPDetails>,
  val nextReviewDate: String,
  val daysSinceReview: Int,
) {
  fun toIEPLevel(): IEPLevel = IEPLevel(iepCode = this.iepCode, iepLevel = this.iepLevel)
}

data class IncIEPDetails(
  val id: Int,
  val iepCode: String,
  val iepLevel: String,
  val comments: String?,
  val prisonerNumber: String,
  val bookingId: Long,
  val iepDate: String,
  val iepTime: String,
  val agencyId: String,
  val userId: String,
  val reviewType: String,
  val auditModuleName: String,
  val isRealReview: Boolean,
)
