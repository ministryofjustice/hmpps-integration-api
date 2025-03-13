package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.alerts

data class AlertsPaginatedAlerts(
  val totalElements: Int,
  val totalPages: Int,
  val first: Boolean,
  val last: Boolean,
  val size: Int,
  val content: List<AlertsAlert>,
  val number: Int,
  val sort: AlertsSort,
  val numberOfElements: Int,
  val pageable: AlertsPageable,
  val empty: Boolean,
)
