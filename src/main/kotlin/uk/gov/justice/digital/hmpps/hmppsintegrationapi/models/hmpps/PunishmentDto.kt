package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class PunishmentDto(
  val type: String? = null,
  val privilegeType: String? = null,
  val otherPrivilege: String? = null,
  val schedule: PunishmentScheduleDto? = null,
)
