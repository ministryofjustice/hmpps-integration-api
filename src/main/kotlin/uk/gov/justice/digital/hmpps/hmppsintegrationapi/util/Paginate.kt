package uk.gov.justice.digital.hmpps.hmppsintegrationapi.util

import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable

infix fun <T> List<T>.paginateWith(paginationOptions: Pageable): PageImpl<T> {
  val start = paginationOptions.pageSize * paginationOptions.pageNumber
  val end = (start + paginationOptions.pageSize).coerceAtMost(this.size)

  if (start > end) {
    return PageImpl(listOf<T>(), paginationOptions, this.size.toLong())
  }
  return PageImpl(this.subList(start, end), paginationOptions, this.count().toLong())
}
