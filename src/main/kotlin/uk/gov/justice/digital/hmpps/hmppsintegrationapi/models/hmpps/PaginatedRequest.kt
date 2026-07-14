package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class PaginatedRequest(
  val page: Int = 1,
  val perPage: Int = 10,
)
