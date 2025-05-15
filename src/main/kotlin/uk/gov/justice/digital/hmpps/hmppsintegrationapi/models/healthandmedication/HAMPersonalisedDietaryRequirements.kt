package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.healthandmedication

data class HAMPersonalisedDietaryRequirements(
  val value: List<HAMPersonalisedDietaryRequirement>,
  val lastModifiedAt: String,
  val lastModifiedBy: String,
  val lastModifiedPrisonId: String,
)

data class HAMPersonalisedDietaryRequirement(
  val value: HAMReferenceDataValue,
  val comment: String?,
)
