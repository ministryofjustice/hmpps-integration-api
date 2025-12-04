package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch

data class POSPaginatedPrisoners(
  val content: List<POSPrisoner>,
  val totalElements: Long,
  val totalPages: Int,
  val first: Boolean,
  val last: Boolean,
  val size: Int,
  val number: Int,
  val sort: POSSort,
  val numberOfElements: Int,
  val pageable: POSPageable,
  val empty: Boolean,
) {
  fun toPOSPrisoners(): List<POSPrisoner> = this.content.sortedByDescending { it.dateOfBirth }
}
