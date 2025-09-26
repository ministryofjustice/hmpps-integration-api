package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.healthandmedication

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Diet

data class HAMDietAndAllergy(
  val foodAllergies: HAMFoodAllergies,
  val medicalDietaryRequirements: HAMMedicalDietaryRequirements,
  val personalisedDietaryRequirements: HAMPersonalisedDietaryRequirements,
  val cateringInstructions: HAMCateringInstructions,
) {
  fun toDiet(): Diet =
    Diet(
      foodAllergies = this.foodAllergies.value.map { it.toFoodAllergy() },
      medicalDietaryRequirements = this.medicalDietaryRequirements.value.map { it.toMedicalDietaryRequirement() },
      personalisedDietaryRequirements = this.personalisedDietaryRequirements.value.map { it.toPersonalisedDietaryRequirement() },
      cateringInstructions = this.cateringInstructions.toCateringInstruction(),
    )
}
