package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.healthandmedication

data class HAMDietAndAllergy(
  val foodAllergies: HAMFoodAllergies,
  val medicalDietaryRequirements: HAMMedicalDietaryRequirements,
  val personalisedDietaryRequirements: HAMPersonalisedDietaryRequirements,
  val cateringInstructions: HAMCateringInstructions,
)
