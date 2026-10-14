package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.tier

data class OASysInputs(
  val predictors: AllPredictorVersioned? = null,
  val everCommittedSexualOffence: Boolean? = null,
)
