package uk.gov.justice.digital.hmpps.hmppsintegrationapi.util

import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError

fun <T> List<T>.paginateWith(page: Int = 1, perPage: Int = 10): PaginatedResponse<T> {
  val paginationOptions = PageRequest.of(page - 1, perPage)
  val start = paginationOptions.pageSize * paginationOptions.pageNumber
  val end = (start + paginationOptions.pageSize).coerceAtMost(this.size)

  if (start > end) {
    return PaginatedResponse(PageImpl(listOf<T>(), paginationOptions, this.size.toLong()))
  }

  return PaginatedResponse(PageImpl(this.subList(start, end), paginationOptions, this.count().toLong()))
}

fun <T> List<T>.paginateWith(page: Int = 1, perPage: Int = 10, errors: List<UpstreamApiError> = emptyList()): PaginatedResponse<T> {
  val paginationOptions = PageRequest.of(page - 1, perPage)
  val start = paginationOptions.pageSize * paginationOptions.pageNumber
  val end = (start + paginationOptions.pageSize).coerceAtMost(this.size)

  if (start > end) {
    return PaginatedResponse(PageImpl(listOf<T>(), paginationOptions, this.size.toLong()), errors)
  }

  return PaginatedResponse(PageImpl(this.subList(start, end), paginationOptions, this.count().toLong()), errors)
}
