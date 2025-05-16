package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.healthandmedication

data class HAMMedicalDietaryRequirements(
  val value: List<HAMMedicalDietaryRequirement>,
  val lastModifiedAt: String,
  val lastModifiedBy: String,
  val lastModifiedPrisonId: String,
)

data class HAMMedicalDietaryRequirement(
  val value: HAMReferenceDataValue,
  val comment: String?,
)
