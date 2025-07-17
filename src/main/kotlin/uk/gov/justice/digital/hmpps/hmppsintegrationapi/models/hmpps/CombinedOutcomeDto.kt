package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class CombinedOutcomeDto(
  @Schema(description = "The outcome")
  val outcome: OutcomeDto? = null,
  @Schema(description = "The optional referral outcome")
  val referralOutcome: OutcomeDto? = null,
)
