package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class PunishmentCommentDto(
  @Schema(description = "Punishment comment")
  val comment: String? = null,
  @Schema(description = "Reason for change")
  val reasonForChange: String? = null,
  @Schema(description = "Date and time comment was created or updated", example = "2025-01-01")
  val dateTime: String? = null,
)
