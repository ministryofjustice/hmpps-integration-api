package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class StatusInformation(
  @Schema(example = "WRSM")
  val code: String? = null,
  @Schema(example = "Warrant/Summons - Outstanding warrant or summons")
  val description: String? = null,
  @Schema(example = "2022-01-01")
  val startDate: String? = null,
  @Schema(example = "2025-01-01")
  val reviewDate: String? = null,
  @Schema(example = "This is a note")
  val notes: String? = null,
)
