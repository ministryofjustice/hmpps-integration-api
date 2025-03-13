package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonerAlerts

data class PAAlertCode(
  val alertTypeCode: String,
  val alertTypeDescription: String,
  val code: String,
  val description: String,
)
