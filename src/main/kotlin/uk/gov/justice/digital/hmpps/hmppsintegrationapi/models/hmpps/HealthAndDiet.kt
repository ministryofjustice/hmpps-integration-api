package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class HealthAndDiet(
  val diet: Diet,
  val smoking: Smoker?,
)

data class Diet(
  val foodAllergies: List<FoodAllergy>,
  val medicalDietaryRequirements: List<MedicalDietaryRequirement>,
  val personalisedDietaryRequirements: List<PersonalisedDietaryRequirement>,
  val cateringInstructions: CateringInstruction,
)

data class Smoker(
  @Schema(description = "Smoker status. Y = Yes, N = No, V = Vapes", example = "Y, N, V")
  val smoking: String?,
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
