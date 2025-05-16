package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.healthandmedication

data class HAMFoodAllergies(
  val value: List<HAMFoodAllergy>,
  val lastModifiedAt: String,
  val lastModifiedBy: String,
  val lastModifiedPrisonId: String,
)

data class HAMFoodAllergy(
  val value: HAMReferenceDataValue,
  val comment: String?,
)
