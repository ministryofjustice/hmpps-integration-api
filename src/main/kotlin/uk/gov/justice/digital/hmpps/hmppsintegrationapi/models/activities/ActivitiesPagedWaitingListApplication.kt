package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PaginatedWaitingListApplications

data class ActivitiesPagedWaitingListApplication(
  val totalPages: Int?,
  val totalElements: Int?,
  val first: Boolean?,
  val last: Boolean?,
  val size: Int?,
  val content: List<ActivitiesWaitingListApplication>?,
  val number: Int?,
  val sort: ActivitiesSort?,
  val numberOfElements: Int?,
  val pageable: ActivitiesPageable?,
  val empty: Boolean?,
) {
  fun toPaginatedWaitingListApplications(): PaginatedWaitingListApplications =
    PaginatedWaitingListApplications(
      content = content?.map { it.toWaitingListApplication() }.orEmpty(),
      totalPages = totalPages ?: 0,
      totalCount = totalElements?.toLong() ?: 0L,
      isLastPage = last ?: true,
      count = numberOfElements ?: 0,
      page = (number ?: 0) + 1,
      perPage = pageable?.pageSize ?: 0,
    )
}
