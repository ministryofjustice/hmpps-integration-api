package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class CombinedOutcomeDto(
  val outcome: OutcomeDto? = null,
  val referralOutcome: OutcomeDto? = null,
)
