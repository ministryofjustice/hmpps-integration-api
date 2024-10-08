package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class RiskAssessment(
  @Schema(description = "The classification code of the risk", example = "C")
  val classificationCode: String? = null,
  @Schema(description = "The classification of the code", example = "Cat C")
  val classification: String? = null,
  @Schema(description = "The assessment code", example = "CATEGORY")
  val assessmentCode: String? = null,
  @Schema(description = "The description of the assessment", example = "Categorisation")
  val assessmentDescription: String? = null,
  @Schema(description = "The date of the assessment", example = "2018-02-11")
  val assessmentDate: String? = null,
  @Schema(description = "Next review date", example = "2018-02-11")
  val nextReviewDate: String? = null,
  @Schema(description = "Agency ID of the assessment", example = "MDI")
  val assessmentAgencyId: String? = null,
  @Schema(description = "The status of the assessment", example = "P")
  val assessmentStatus: String? = null,
  @Schema(description = "Comments regarding the assessment", example = "Comment details")
  val assessmentComment: String? = null,
)
