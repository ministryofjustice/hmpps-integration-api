package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships

data class PRNumberOfChildren(
  val id: Int,
  val numberOfChildren: String,
  val active: Boolean,
  val createdTime: String,
  val createdBy: String,
)
