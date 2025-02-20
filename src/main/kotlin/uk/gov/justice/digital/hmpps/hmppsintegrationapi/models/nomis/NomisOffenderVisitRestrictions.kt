package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonVisitRestriction

data class NomisOffenderVisitRestrictions(
  val bookingId: Long,
  val offenderRestrictions: List<OffenderRestriction>,
)

data class OffenderRestriction(
  val restrictionId: Long,
  val comment: String,
  val restrictionType: String,
  val restrictionTypeDescription: String,
  val startDate: String,
  val expiryDate: String? = null,
  val active: Boolean,
) {
  fun toPersonVisitRestriction() =
    PersonVisitRestriction(
      restrictionId = this.restrictionId,
      comment = this.comment,
      restrictionType = this.restrictionType,
      restrictionTypeDescription = this.restrictionTypeDescription,
      startDate = this.startDate,
      expiryDate = this.expiryDate,
      active = this.active,
    )
}
