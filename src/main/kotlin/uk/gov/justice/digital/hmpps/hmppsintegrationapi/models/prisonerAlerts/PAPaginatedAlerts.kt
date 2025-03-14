package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonerAlerts

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PaginatedAlerts

data class PAPaginatedAlerts(
  val content: List<PAAlert>,
  val totalElements: Long,
  val totalPages: Int,
  val first: Boolean,
  val last: Boolean,
  val size: Int,
  val number: Int,
  val sort: PASort,
  val numberOfElements: Int,
  val pageable: PAPageable,
  val empty: Boolean,
) {
  fun toPaginatedAlerts() =
    PaginatedAlerts(
      content = this.content.map { it.toAlert() },
      totalPages = this.totalPages,
      totalCount = this.totalElements,
      isLastPage = this.last,
      count = this.numberOfElements,
      page = this.number,
      perPage = this.size,
    )
}
