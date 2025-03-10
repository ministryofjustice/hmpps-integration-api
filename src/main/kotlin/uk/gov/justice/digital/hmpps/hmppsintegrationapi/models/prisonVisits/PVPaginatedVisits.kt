package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonVisits

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PaginatedVisits

data class PVPaginatedVisits(
  val content: List<PVVisit>,
  val totalPages: Int,
  val totalCount: Long,
  val isLastPage: Boolean,
  val count: Int,
  val page: Int,
  val perPage: Int,
) {
  fun toPaginatedVisits(): PaginatedVisits =
    PaginatedVisits(
      content = this.content.map { it.toVisit() },
      totalPages = this.totalPages,
      totalCount = this.totalCount,
      isLastPage = this.isLastPage,
      count = this.count,
      page = this.page,
      perPage = this.perPage,
    )
}
