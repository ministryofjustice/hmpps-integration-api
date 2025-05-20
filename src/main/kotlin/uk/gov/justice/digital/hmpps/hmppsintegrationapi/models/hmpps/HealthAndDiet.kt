package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class HealthAndDiet(
  @Schema(description = "Diet information for a prisoner, specifically relating to any allergies or dietary requirements.")
  val diet: Diet? = null,
  @Schema(description = "Details of whether a prisoner smokes or vapes.", example = "Y= Yes, N=No, V=Vapes")
  val smoking: String? = null,
)

data class Diet(
  @Schema(description = "List of food allergies for a prisoner.")
  val foodAllergies: List<FoodAllergy>,
  @Schema(description = "List of medical dietary requirements for a prisoner.")
  val medicalDietaryRequirements: List<MedicalDietaryRequirement>,
  @Schema(description = "List of personalised dietary requirements for a prisoner.")
  val personalisedDietaryRequirements: List<PersonalisedDietaryRequirement>,
  @Schema(description = "Catering instructions for a prisoner.")
  val cateringInstructions: CateringInstruction,
)

data class FoodAllergy(
  @Schema(description = "ID of the allergy.", example = "FOOD_ALLERGY_MILK")
  val id: String,
  @Schema(description = "Code of the allergy.", example = "MILK")
  val code: String,
  @Schema(description = "Description of the allergy.", example = "Milk")
  val description: String,
  @Schema(description = "Any other comments relating to the allergy.", example = "Will need oat milk instead.")
  val comment: String? = null,
)

data class MedicalDietaryRequirement(
  @Schema(description = "ID of the dietary requirement.", example = "FOOD_ALLERGY_MILK")
  val id: String,
  @Schema(description = "Code of the dietary requirement.", example = "MILK")
  val code: String,
  @Schema(description = "Description of the dietary requirement.", example = "Milk")
  val description: String,
  @Schema(description = "Any other comments relating to the dietary requirement.", example = "Will need oat milk instead.")
  val comment: String? = null,
)

data class PersonalisedDietaryRequirement(
  @Schema(description = "ID of the dietary requirement.", example = "FOOD_ALLERGY_MILK")
  val id: String,
  @Schema(description = "Code of the dietary requirement.", example = "MILK")
  val code: String,
  @Schema(description = "Description of the dietary requirement.", example = "Milk")
  val description: String,
  @Schema(description = "Any other comments relating to the dietary requirement.", example = "Will need oat milk instead.")
  val comment: String? = null,
)

data class CateringInstruction(
  @Schema(description = "Catering instructions relating to the prisoner.", example = "catering instruction.")
  val value: String?,
)
