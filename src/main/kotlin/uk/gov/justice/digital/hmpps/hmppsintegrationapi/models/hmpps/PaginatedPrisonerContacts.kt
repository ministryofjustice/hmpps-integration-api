package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class PaginatedPrisonerContacts(
  var contacts: List<PrisonerContact>,
  val isLast: Boolean,
  val numberOfElements: Int,
  val number: Int,
  val size: Int,
  val totalElements: Long,
  val totalPages: Int,
)
