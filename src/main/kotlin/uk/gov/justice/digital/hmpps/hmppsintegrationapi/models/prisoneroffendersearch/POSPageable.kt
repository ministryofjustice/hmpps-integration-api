package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch

data class POSPageable(
  val offset: Long,
  val sort: POSPaginationSort,
  val pageSize: Int,
  val pageNumber: Int,
  val paged: Boolean,
  val unpaged: Boolean,
)
