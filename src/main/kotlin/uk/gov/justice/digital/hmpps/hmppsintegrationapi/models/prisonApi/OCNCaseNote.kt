package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PaginatedCaseNotes

data class OCNCaseNote(
  val content: List<PrisonApiCaseNote> = listOf(),
  @JsonProperty("metadata")
  val page: OCNPagination,
) {
  fun toPaginatedCaseNotes(): PaginatedCaseNotes =
    PaginatedCaseNotes(
      content = this.content.map { it.toCaseNote() },
      count = this.content.size,
      page = this.page.page,
      totalCount = this.page.totalElements.toLong(),
      totalPages = (this.page.totalElements + this.page.size - 1) / this.page.size,
      isLastPage = page.page * page.size >= page.totalElements,
      perPage = this.page.size,
    )
}

data class OCNPagination(
  @Schema(description = "Current Page")
  val page: Int,
  @Schema(description = "Total elements")
  val totalElements: Int,
  @Schema(description = "Records per page")
  val size: Int,
)
