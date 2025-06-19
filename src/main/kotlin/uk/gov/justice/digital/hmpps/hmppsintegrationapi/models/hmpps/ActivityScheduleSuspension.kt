package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class ActivityScheduleSuspension(
  val suspendedFrom: String,
  val suspendedUntil: String?,
)
