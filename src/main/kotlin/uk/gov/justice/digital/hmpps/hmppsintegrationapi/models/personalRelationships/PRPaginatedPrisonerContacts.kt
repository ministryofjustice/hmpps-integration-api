package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships

import com.fasterxml.jackson.annotation.JsonProperty
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PaginatedPrisonerContacts

data class PRPaginatedPrisonerContacts(
  @JsonProperty("content")
  val contacts: List<PRPrisonerContact>,
  val pageable: Pageable,
  val totalElements: Long,
  val totalPages: Long,
  val first: Boolean,
  val last: Boolean,
  val size: Long,
  val number: Long,
  val sort: Sort,
  val numberOfElements: Long,
  val empty: Boolean,
) {
  fun toPaginatedPrisonerContacts(): PaginatedPrisonerContacts =
    PaginatedPrisonerContacts(
      content = this.contacts.map { it.toPrisonerContact() },
      isLastPage = this.last,
      count = this.numberOfElements.toInt(),
      page = this.number.toInt(),
      perPage = this.size.toInt(),
      totalCount = this.totalElements,
      totalPages = this.totalPages.toInt(),
    )
}

data class Sort(
  val empty: Boolean,
  val sorted: Boolean,
  val unsorted: Boolean,
)

data class Pageable(
  val offset: Long,
  val sort: Sort,
  val pageSize: Long,
  val paged: Boolean,
  val pageNumber: Long,
  val unpaged: Boolean,
)
