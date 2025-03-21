package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonerAlerts

data class PAPageable(
  val offset: Long,
  val sort: PASort,
  val unpaged: Boolean,
  val pageSize: Int,
  val paged: Boolean,
  val pageNumber: Int,
)
