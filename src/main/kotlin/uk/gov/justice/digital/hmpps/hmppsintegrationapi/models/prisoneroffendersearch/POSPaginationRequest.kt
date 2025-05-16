package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch

data class POSPaginationRequest(
  val page: Int = 0, // Pagination is 0 based
  val size: Int = 10,
)
