package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.NumberOfChildren

data class PRNumberOfChildren(
  val id: Int,
  val numberOfChildren: String?,
  val active: Boolean,
  val createdTime: String?,
  val createdBy: String?,
) {
  fun toNumberOfChildren(): NumberOfChildren =
    NumberOfChildren(
      numberOfChildren = this.numberOfChildren,
    )
}
