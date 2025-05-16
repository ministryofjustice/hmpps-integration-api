package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class HealthAndDiet(
  val diet: Diet? = null,
  val smoking: String? = null,
)

data class Diet(
  val foodAllergies: List<FoodAllergy>,
  val medicalDietaryRequirements: List<MedicalDietaryRequirement>,
  val personalisedDietaryRequirements: List<PersonalisedDietaryRequirement>,
  val cateringInstructions: CateringInstruction,
)

data class FoodAllergy(
  val id: String,
  val code: String,
  val description: String,
  val comment: String? = null,
)

data class MedicalDietaryRequirement(
  val id: String,
  val code: String,
  val description: String,
  val comment: String? = null,
)

data class PersonalisedDietaryRequirement(
  val id: String,
  val code: String,
  val description: String,
  val comment: String? = null,
)

data class CateringInstruction(
  val value: String?,
)
