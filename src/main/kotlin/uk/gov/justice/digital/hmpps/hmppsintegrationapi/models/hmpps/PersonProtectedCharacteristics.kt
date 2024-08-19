package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

class PersonProtectedCharacteristics(
  @Schema(description = "Age of the person", example = "35")
  val age: Number,
  @Schema(description = "Gender of the person", example = "Female")
  val gender: String?,
  @Schema(description = "Sexual orientation of the person", example = "Unknown")
  val sexualOrientation: String?,
  @Schema(description = "Ethnicity of the person", example = "White: Eng./Welsh/Scot./N.Irish/British")
  val ethnicity: String?,
  @Schema(description = "Nationality of the person", example = "Egyptian")
  val nationality: String?,
  @Schema(description = "Religion of the person", example = "Church of England (Anglican)")
  val religion: String?,
  val disabilities: List<Disability>,
  @Schema(description = "Marital status of the person", example = "Widowed")
  var maritalStatus: String? = null,
  var reasonableAdjustments: List<ReasonableAdjustment> = emptyList(),
)
