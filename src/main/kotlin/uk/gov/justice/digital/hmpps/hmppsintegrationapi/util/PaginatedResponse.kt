package uk.gov.justice.digital.hmpps.hmppsintegrationapi.util

import org.springframework.data.domain.Page
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.BaseResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError

class PaginatedResponse<T>(pageableResponse: Page<T>, override val errors: List<UpstreamApiError>) : BaseResponse(errors) {

  constructor(pageableResponse: Page<T>) : this(pageableResponse, emptyList())
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
