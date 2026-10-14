package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.tier

data class AssessmentForTier(
  val assessment: AssessmentSummary? = null,
  val accommodation: NeedSection? = null,
  val educationTrainingEmployability: NeedSection? = null,
  val relationships: NeedSection? = null,
  val lifestyleAndAssociates: NeedSection? = null,
  val drugMisuse: NeedSection? = null,
  val alcoholMisuse: NeedSection? = null,
  val thinkingAndBehaviour: NeedSection? = null,
  val attitudes: NeedSection? = null,
)
