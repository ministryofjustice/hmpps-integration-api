package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class PersonalCareNeed(
  @Schema(description = "Type of the problem", example = "MATSTAT")
  val problemType: String? = null,
  @Schema(description = "Code of the problem", example = "ACCU9")
  val problemCode: String? = null,
  @Schema(description = "Status of the problem", example = "ON")
  val problemStatus: String? = null,
  @Schema(description = "Detailed description of the problem", example = "No Disability")
  val problemDescription: String? = null,
  @Schema(description = "Additional comments or notes", example = "COMMENT")
  val commentText: String? = null,
  @Schema(description = "Start date for the problem", example = "2020-06-21")
  val startDate: String? = null,
  @Schema(description = "End date for the problem", example = "null")
  val endDate: String? = null,
)
