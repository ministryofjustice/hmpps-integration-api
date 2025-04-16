package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.interfaces.IPaginatedObject

data class PaginatedCaseNotes(
  override val content: List<CaseNote>,
  override val isLastPage: Boolean,
  override val count: Int,
  override val page: Int,
  override val perPage: Int,
  override val totalCount: Long,
  override val totalPages: Int,
) : IPaginatedObject<CaseNote>
