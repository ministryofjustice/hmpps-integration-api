package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships

data class NumberOfChildren(
  val id: Int,
  val numberOfChildren: String,
  val active: Boolean,
  val createdTime: String,
  val createdBy: String,
)
