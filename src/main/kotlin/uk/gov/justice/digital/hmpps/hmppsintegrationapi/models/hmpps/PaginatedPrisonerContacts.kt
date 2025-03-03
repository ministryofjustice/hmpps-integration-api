package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.interfaces.IPaginatedObject

data class PaginatedPrisonerContacts(
  override val content: List<PrisonerContact>,
  override val isLastPage: Boolean,
  override val count: Int,
  override val page: Int,
  override val perPage: Int,
  override val totalCount: Long,
  override val totalPages: Int,
) : IPaginatedObject<PrisonerContact>
