package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.manageUsers

data class PaginatedUsers(
  val content: List<HmppsAuthUser>,
  val totalPages: Int? = null,
  val totalElements: Long? = null,
  val last: Boolean? = null,
  val size: Int? = null,
  val number: Int? = null,
  val perPage: Int? = null,
)
