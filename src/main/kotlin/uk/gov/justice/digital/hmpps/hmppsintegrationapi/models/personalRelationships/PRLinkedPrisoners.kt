package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.web.PagedModel
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PaginatedContactLinkedPrisonerResponse

data class PRLinkedPrisoners(
  @JsonProperty("content")
  val prisoners: List<PRLinkedPrisoner>,
  @JsonProperty("page")
  val pageMetadata: PagedModel.PageMetadata,
) {
  fun toPaginatedLinkedPrisonerResponse(): PaginatedContactLinkedPrisonerResponse =
    PaginatedContactLinkedPrisonerResponse(
      content = this.prisoners.map { it.toLinkedPrisoner() },
      count = this.prisoners.size,
      page = this.pageMetadata.number.toInt() + 1,
      totalCount = this.pageMetadata.totalElements,
      totalPages = this.pageMetadata.totalPages.toInt(),
      isLastPage = (this.pageMetadata.totalPages.toInt() == 0 || this.pageMetadata.number + 1 == this.pageMetadata.totalPages),
      perPage = this.pageMetadata.size.toInt(),
    )
}
