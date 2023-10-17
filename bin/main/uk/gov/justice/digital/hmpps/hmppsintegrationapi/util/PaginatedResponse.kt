package uk.gov.justice.digital.hmpps.hmppsintegrationapi.util

import org.springframework.data.domain.Page

class PaginatedResponse<T>(pageableResponse: Page<T>) {
  val data: List<T> = pageableResponse.content
  val pagination: Pagination = Pagination(pageableResponse)

  inner class Pagination(pageableResponse: Page<T>) {
    val isLastPage: Boolean = pageableResponse.isLast
    val count: Int = pageableResponse.numberOfElements
    val page: Int = pageableResponse.number + 1
    val perPage: Int = pageableResponse.size
    val totalCount: Long = pageableResponse.totalElements
    val totalPages: Int = pageableResponse.totalPages
  }
}
