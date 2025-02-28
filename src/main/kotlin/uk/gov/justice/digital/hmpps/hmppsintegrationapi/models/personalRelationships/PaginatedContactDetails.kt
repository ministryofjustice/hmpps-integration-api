package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonerContact

// maybe json properties idk
class PaginatedContactDetails(
  var contacts: List<PrisonerContact>? = null,
  val isLast: Boolean,
  val numberOfElements: Int,
  val number: Int,
  val size: Int,
  val totalElements: Long,
  val totalPages: Int,
)
