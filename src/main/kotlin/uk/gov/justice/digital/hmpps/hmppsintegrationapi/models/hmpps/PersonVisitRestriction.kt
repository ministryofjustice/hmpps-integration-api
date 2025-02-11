package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class PersonVisitRestriction(
  val restrictionId: Long,
  val comment: String,
  val restrictionType: String,
  val restrictionTypeDescription: String,
  val startDate: String,
  val expiryDate: String,
  val active: Boolean,
)
