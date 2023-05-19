package uk.gov.justice.digital.hmpps.hmppsintegrationapi.util

import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable

infix fun <T> List<T>.paginateWith(pageable: Pageable): PageImpl<T> {
  val start = pageable.pageSize * pageable.pageNumber
  val end = (start + pageable.pageSize).coerceAtMost(this.size)

  if (start > end) {
    return PageImpl(listOf<T>(), pageable, this.size.toLong())
  }
  return PageImpl(this.subList(start, end), pageable, this.count().toLong())
}
