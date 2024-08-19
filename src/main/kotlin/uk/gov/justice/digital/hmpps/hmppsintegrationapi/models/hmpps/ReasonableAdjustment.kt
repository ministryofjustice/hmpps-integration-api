package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

class ReasonableAdjustment(
  @Schema(description = "Treatment code", example = "WHEELCHR_ACC")
  val treatmentCode: String? = null,
  @Schema(description = "Comment text", example = "abcd")
  val commentText: String? = null,
  @Schema(description = "Start date", example = "2013-04-11")
  val startDate: LocalDate? = null,
  @Schema(description = "End date", example = "2023-04-11")
  val endDate: LocalDate? = null,
  @Schema(description = "Treatment description", example = "Wheelchair accessibility")
  val treatmentDescription: String? = null,
)
