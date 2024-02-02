package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class PunishmentScheduleDto(
  val days: Number? = null,
  val startDate: String? = null,
  val endDate: String? = null,
  val suspendedUntil: String? = null,
)
