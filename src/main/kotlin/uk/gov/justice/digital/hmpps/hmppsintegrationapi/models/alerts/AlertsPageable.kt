package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.alerts

data class AlertsPageable(
  val offset: Long,
  val sort: AlertsSort,
  val unpaged: Boolean,
  val pageSize: Int,
  val paged: Boolean,
  val pageNumber: Int,
)
