package uk.gov.justice.digital.hmpps.hmppsintegrationapi.util

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.data.domain.Page

class PaginatedResponse<T>(
  @Schema(hidden = true) pageableResponse: Page<T>,
) {
  val data: List<T> = pageableResponse.content
  val pagination: Pagination = Pagination(pageableResponse)

  inner class Pagination(
    pageableResponse: Page<T>,
  ) {
    @Schema(description = "Is the current page the last one?", example = "true")
    val isLastPage: Boolean = pageableResponse.isLast

    @Schema(description = "The number of results in `data` for the current page", example = "1")
    val count: Int = pageableResponse.numberOfElements

    @Schema(description = "The current page number", example = "1")
    val page: Int = pageableResponse.number + 1

    @Schema(description = "The maximum number of results in `data` for a page", example = "10")
    val perPage: Int = pageableResponse.size

    @Schema(description = "The total number of results in `data` across all pages", example = "1")
    val totalCount: Long = pageableResponse.totalElements

    @Schema(description = "The total number of pages", example = "1")
    val totalPages: Int = pageableResponse.totalPages
  }
}
