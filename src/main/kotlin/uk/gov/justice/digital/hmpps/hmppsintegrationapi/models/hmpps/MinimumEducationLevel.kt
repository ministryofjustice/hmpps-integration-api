package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class MinimumEducationLevel(
  @Schema(description = "The education level code", example = "Basic")
  val educationLevelCode: String,
  @Schema(description = "The education level description", example = "Basic")
  val educationLevelDescription: String,
  @Schema(description = "The study area code", example = "ENGLA")
  val studyAreaCode: String,
  @Schema(description = "The study area description", example = "English Language")
  val studyAreaDescription: String,
)
