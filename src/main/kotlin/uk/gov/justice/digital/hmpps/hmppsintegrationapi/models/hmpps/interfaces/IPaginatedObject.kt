package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.interfaces

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.PaginatedResponse

interface IPaginatedObject<T> {
  val content: List<T>
  val isLastPage: Boolean
  val count: Int
  val page: Int
  val perPage: Int
  val totalCount: Long
  val totalPages: Int
}

fun <T> IPaginatedObject<T>?.toPaginatedResponse(): PaginatedResponse<T> = PaginatedResponse.fromPaginatedObject(this)
