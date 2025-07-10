package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class SuitabilityCriteria(
  @Schema(description = "The most recent risk assessment level for this activity", example = "high")
  val riskLevel: String,
  @Schema(description = "Whether the activity is a paid activity", example = "true")
  val isPaid: Boolean,
  @Schema(description = "The pay rate by incentive level and pay band that can apply to this activity")
  val payRate: List<PayRate>,
  @Schema(description = "The list of minimum education levels that can apply to this activity")
  val minimumEducationLevel: List<MinimumEducationLevel>,
)
