package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class PunishmentDto(
  @Schema(description = "Punishment type", example = "PRIVILEGE")
  val type: String? = null,
  @Schema(description = "Privelege type", example = "CANTEEN")
  val privilegeType: String? = null,
  @Schema(description = "Other privelege type")
  val otherPrivilege: String? = null,
  @Schema(description = "Latest punishment schedule")
  val schedule: PunishmentScheduleDto? = null,
)
