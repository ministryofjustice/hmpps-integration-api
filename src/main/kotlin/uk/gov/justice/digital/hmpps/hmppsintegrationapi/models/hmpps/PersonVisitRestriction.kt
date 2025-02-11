package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class PersonVisitRestriction(
  @Schema(description = "Restriction ID")
  val restrictionId: Long,
  @Schema(description = "Restriction comment text")
  val comment: String,
  @Schema(description = "Code of restriction type")
  val restrictionType: String,
  @Schema(description = "Description of restriction type")
  val restrictionTypeDescription: String,
  @Schema(description = "Date from which the restrictions applies", example = "1980-01-01")
  val startDate: String,
  @Schema(description = "Date restriction applies to, or indefinitely if null", example = "1980-01-01")
  val expiryDate: String? = null,
  @Schema(description = "True if restriction is within the start date and optional expiry date range")
  val active: Boolean,
)
