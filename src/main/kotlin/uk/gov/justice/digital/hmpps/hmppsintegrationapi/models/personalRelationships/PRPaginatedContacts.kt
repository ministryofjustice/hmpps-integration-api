package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships

import com.fasterxml.jackson.annotation.JsonProperty

data class PRPaginatedContacts(
  @JsonProperty("content")
  val contacts: List<PRContact?>,
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
)

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
