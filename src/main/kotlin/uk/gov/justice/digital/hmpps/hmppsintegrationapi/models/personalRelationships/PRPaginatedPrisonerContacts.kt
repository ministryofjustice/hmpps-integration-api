package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.web.PagedModel
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PaginatedPrisonerContacts

data class PRPaginatedPrisonerContacts(
  @JsonProperty("content")
  val contacts: List<PRPrisonerContact>,
  @JsonProperty("page")
  val pageMetadata: PagedModel.PageMetadata,
) {
  fun toPaginatedPrisonerContacts(): PaginatedPrisonerContacts =
    PaginatedPrisonerContacts(
      content = this.contacts.map { it.toPrisonerContact() },
      count = this.pageMetadata.size.toInt(),
      page = this.pageMetadata.number.toInt() + 1,
      totalCount = this.pageMetadata.totalElements,
      totalPages = this.pageMetadata.totalPages.toInt(),
      isLastPage = this.pageMetadata.number + 1 == this.pageMetadata.totalPages,
      perPage = this.pageMetadata.size.toInt(),
    )
}
