package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.healthandmedication

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonalisedDietaryRequirement

data class HAMPersonalisedDietaryRequirements(
  val value: List<HAMPersonalisedDietaryRequirement>,
  val lastModifiedAt: String,
  val lastModifiedBy: String,
  val lastModifiedPrisonId: String,
)

data class HAMPersonalisedDietaryRequirement(
  val value: HAMReferenceDataValue,
  val comment: String?,
) {
  fun toPersonalisedDietaryRequirement() =
    PersonalisedDietaryRequirement(
      id = this.value.id,
      code = this.value.code,
      description = this.value.description,
      comment = this.comment,
    )
}
