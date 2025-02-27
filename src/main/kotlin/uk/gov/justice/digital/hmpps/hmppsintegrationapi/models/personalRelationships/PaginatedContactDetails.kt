package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships

// maybe json properties idk
class PaginatedContactDetails(
  var contacts: List<ContactDetails>? = null,
  val isLast: Boolean,
  val numberOfElements: Int,
  val number: Int,
  val size: Int,
  val totalElements: Long,
  val totalPages: Int,
)
