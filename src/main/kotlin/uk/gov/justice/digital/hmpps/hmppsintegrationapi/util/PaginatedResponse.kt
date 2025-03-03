package uk.gov.justice.digital.hmpps.hmppsintegrationapi.util

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.data.domain.Page
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.interfaces.IPaginatedObject

class PaginatedResponse<T>(
  val data: List<T>,
  val pagination: Pagination,
) {
  companion object {
    fun <T> fromPageableResponse(pageableResponse: Page<T>): PaginatedResponse<T> {
      val data: List<T> = pageableResponse.content
      val pagination =
        Pagination(
          isLastPage = pageableResponse.isLast,
          count = pageableResponse.numberOfElements,
          page = pageableResponse.number + 1,
          perPage = pageableResponse.size,
          totalCount = pageableResponse.totalElements,
          totalPages = pageableResponse.totalPages,
        )
      return PaginatedResponse(data, pagination)
    }

    fun <T> fromPaginatedObject(paginatedObject: IPaginatedObject<T>?): PaginatedResponse<T> {
      if (paginatedObject == null) {
        return PaginatedResponse(
          emptyList(),
          Pagination(
            isLastPage = true,
            count = 0,
            page = 0,
            perPage = 0,
            totalCount = 0,
            totalPages = 0,
          ),
        )
      }
      return PaginatedResponse(
        data = paginatedObject.content,
        Pagination(
          isLastPage = paginatedObject.isLastPage,
          count = paginatedObject.count,
          page = paginatedObject.page,
          perPage = paginatedObject.perPage,
          totalCount = paginatedObject.totalCount,
          totalPages = paginatedObject.totalPages,
        ),
      )
    }
  }
}

data class Pagination(
  @Schema(description = "Is the current page the last one?", example = "true")
  val isLastPage: Boolean,
  @Schema(description = "The number of results in `data` for the current page", example = "1")
  val count: Int,
  @Schema(description = "The current page number", example = "1")
  val page: Int,
  @Schema(description = "The maximum number of results in `data` for a page", example = "10")
  val perPage: Int,
  @Schema(description = "The total number of results in `data` across all pages", example = "1")
  val totalCount: Long,
  @Schema(description = "The total number of pages", example = "1")
  val totalPages: Int,
)
