package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.alerts

data class AlertsAlertCode(
  val alertTypeCode: String,
  val alertTypeDescription: String,
  val code: String,
  val description: String,
)
