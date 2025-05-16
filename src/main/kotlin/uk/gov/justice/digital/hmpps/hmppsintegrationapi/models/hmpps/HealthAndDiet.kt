package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

class HealthAndDiet(
  val diet: Diet,
  val smoking: Smoker,
)

class Diet(
  val foodAllergies: List<FoodAllergy>,
  val medicalDietaryRequirements: List<MedicalDietaryRequirement>,
  val personalisedDietaryRequirements: List<PersonalisedDietaryRequirement>,
  val cateringInstructions: CateringInstruction,
)

class Smoker(
  val smoking: String,
)

class FoodAllergy(
  val id: String,
  val code: String,
  val description: String,
  val comment: String? = null,
)

class MedicalDietaryRequirement(
  val id: String,
  val code: String,
  val description: String,
  val comment: String? = null,
)

class PersonalisedDietaryRequirement(
  val id: String,
  val code: String,
  val description: String,
  val comment: String? = null,
)

class CateringInstruction(
  val value: String,
)
