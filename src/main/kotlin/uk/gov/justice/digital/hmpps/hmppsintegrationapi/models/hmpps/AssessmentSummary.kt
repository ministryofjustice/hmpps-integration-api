package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

data class AssessmentSummary(
  @Schema(example = "2026-06-08T14:20:47")
  val initiationDate: LocalDateTime? = null,
  @Schema(example = "2026-06-08T14:20:47")
  val completedDate: LocalDateTime? = null,
  @Schema(example = "Assessment Type")
  val assessmentType: String? = null,
  @Schema(example = "Assessment Status")
  val status: String? = null,
  @Schema(example = "Assessor Name")
  val assessorName: String? = null,
  @Schema(example = "Countersigner Name")
  val countersignerName: String? = null,
)
