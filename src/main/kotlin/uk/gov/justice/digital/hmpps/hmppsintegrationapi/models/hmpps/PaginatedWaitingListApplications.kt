package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.interfaces.IPaginatedObject

data class PaginatedWaitingListApplications(
  override val content: List<WaitingListApplication>,
  override val totalPages: Int,
  override val totalCount: Long,
  override val isLastPage: Boolean,
  override val count: Int,
  override val page: Int,
  override val perPage: Int,
) : IPaginatedObject<WaitingListApplication>
