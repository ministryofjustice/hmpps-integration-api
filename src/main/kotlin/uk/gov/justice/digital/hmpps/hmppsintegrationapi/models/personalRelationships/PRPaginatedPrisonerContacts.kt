package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships

import com.fasterxml.jackson.annotation.JsonProperty
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PaginatedPrisonerContacts

data class PRPaginatedPrisonerContacts(
  @JsonProperty("content")
  val contacts: List<PRPrisonerContact>,
) {
  fun toPaginatedPrisonerContacts(): PaginatedPrisonerContacts =
    PaginatedPrisonerContacts(
      contacts = this.contacts.map { it.toPrisonerContact() },
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
