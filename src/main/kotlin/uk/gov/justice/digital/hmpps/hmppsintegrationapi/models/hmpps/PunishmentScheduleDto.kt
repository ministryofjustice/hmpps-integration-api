package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class PunishmentScheduleDto(
  @Schema(description = "Days punishment will last", example = "2")
  val days: Number? = null,
  @Schema(description = "Start date of punishment", example = "2025-01-01")
  val startDate: String? = null,
  @Schema(description = "End date of punishment", example = "2025-01-03")
  val endDate: String? = null,
  @Schema(description = "Punishment suspeended until date", example = "2025-01-30")
  val suspendedUntil: String? = null,
)
