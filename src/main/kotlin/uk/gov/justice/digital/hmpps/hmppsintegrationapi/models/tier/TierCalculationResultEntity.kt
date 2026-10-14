package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.tier

data class TierCalculationResultEntity(
  val tier: String? = null,
  val provisional: Boolean? = null,
  val calculationVersion: String,
  val deliusInputs: DeliusInputs? = null,
  val assessmentSummary: AssessmentForTier? = null,
  val oasysInputs: OASysInputs? = null,
)
