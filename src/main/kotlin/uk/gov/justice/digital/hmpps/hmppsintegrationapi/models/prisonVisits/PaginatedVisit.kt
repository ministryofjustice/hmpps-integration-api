package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonVisits

import com.fasterxml.jackson.annotation.JsonProperty

// A lot of these fields seem overkill?
data class PaginatedVisit(
  val totalElements: Long,
  val totalPages: Long,
  val first: Boolean,
  val last: Boolean,
  val size: Long,
  val number: Long,
  val sort: Sort,
  val numberOfElements: Long,
  val pageable: Pageable,
  val empty: Boolean,
  @JsonProperty("content")
  val visits: List<Visit?>,
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
