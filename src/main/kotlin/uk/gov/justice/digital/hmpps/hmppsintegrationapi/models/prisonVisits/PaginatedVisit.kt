package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonVisits

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.interfaces.IPaginatedObject

data class PaginatedVisit(
  override val content: List<PVVisit>,
  override val totalPages: Int,
  override val totalCount: Long,
  override val isLastPage: Boolean,
  override val count: Int,
  override val page: Int,
  override val perPage: Int,
) : IPaginatedObject<PVVisit>
