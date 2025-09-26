package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.healthandmedication

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.FoodAllergy

data class HAMFoodAllergies(
  val value: List<HAMFoodAllergy>,
  val lastModifiedAt: String,
  val lastModifiedBy: String,
  val lastModifiedPrisonId: String,
)

data class HAMFoodAllergy(
  val value: HAMReferenceDataValue,
  val comment: String?,
) {
  fun toFoodAllergy() =
    FoodAllergy(
      id = this.value.id,
      code = this.value.code,
      description = this.value.description,
      comment = this.comment,
    )
}
