package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonVisits

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PaginatedVisits

data class PVPaginatedVisits(
  val content: List<PVVisit>,
  val totalPages: Int,
  val totalElements: Long,
  val last: Boolean,
  val size: Int,
  val number: Int,
  val pageable: PVPage,
) {
  fun toPaginatedVisits(): PaginatedVisits =
    PaginatedVisits(
      content = this.content.map { it.toVisit() },
      totalPages = this.totalPages,
      totalCount = this.totalElements,
      isLastPage = this.last,
      count = this.size,
      page = this.number + 1,
      perPage = pageable.pageSize,
    )
}

data class PVPage(
  val pageSize: Int,
)
