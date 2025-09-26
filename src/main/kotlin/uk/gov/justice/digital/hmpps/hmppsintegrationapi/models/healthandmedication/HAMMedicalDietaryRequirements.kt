package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.healthandmedication

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.MedicalDietaryRequirement

data class HAMMedicalDietaryRequirements(
  val value: List<HAMMedicalDietaryRequirement>,
  val lastModifiedAt: String,
  val lastModifiedBy: String,
  val lastModifiedPrisonId: String,
)

data class HAMMedicalDietaryRequirement(
  val value: HAMReferenceDataValue,
  val comment: String?,
) {
  fun toMedicalDietaryRequirement() =
    MedicalDietaryRequirement(
      id = this.value.id,
      code = this.value.code,
      description = this.value.description,
      comment = this.comment,
    )
}
