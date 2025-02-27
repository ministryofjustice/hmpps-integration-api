package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships

import com.fasterxml.jackson.annotation.JsonProperty

data class PRPaginatedContacts(
  @JsonProperty("content")
  val contacts: List<PersonalRelationshipsContactResponse>,
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
  fun toPaginatedContactDetails(): PaginatedContactDetails = PaginatedContactDetails(isLast = this.last, numberOfElements = this.numberOfElements.toInt(), number = this.number.toInt(), size = this.size.toInt(), totalElements = this.totalElements, totalPages = this.totalPages.toInt())
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
