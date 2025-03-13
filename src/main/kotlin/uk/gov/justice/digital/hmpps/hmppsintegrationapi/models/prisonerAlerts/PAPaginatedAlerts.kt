package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonerAlerts

data class PAPaginatedAlerts(
  val totalElements: Int,
  val totalPages: Int,
  val first: Boolean,
  val last: Boolean,
  val size: Int,
  val content: List<PAAlert>,
  val number: Int,
  val sort: PASort,
  val numberOfElements: Int,
  val pageable: PAPageable,
  val empty: Boolean,
)
