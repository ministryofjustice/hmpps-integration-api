package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.csra

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

data class AssessmentSummary(
  @Schema(description = "Booking number", example = "123456")
  val bookingId: Long,
  @Schema(description = "Sequence number of assessment within booking", example = "1")
  val assessmentSeq: Int,
  @Schema(description = "Offender number (e.g. NOMS Number).", example = "GV09876N")
  val offenderNo: String,
  @Schema(description = "Classification code. This will not have a value if the assessment is incomplete or pending", example = "STANDARD")
  val classificationCode: String?,
  @Schema(description = "Identifies the type of assessment", example = "CSR")
  val assessmentCode: String,
  @Schema(description = "Indicates whether this is a CSRA assessment")
  val cellSharingAlertFlag: Boolean,
  @Schema(description = "Date assessment was created", example = "2018-02-11")
  val assessmentDate: LocalDate,
  @Schema(description = "The assessment creation agency id", example = "MDI")
  val assessmentAgencyId: String?,
  @Schema(description = "Comment from assessor", example = "Comment details")
  val assessmentComment: String?,
  @Schema(description = "Username who made the assessment", example = "NGK33Y")
  val assessorUser: String?,
  @Schema(description = "Date of next review", example = "2018-02-11")
  val nextReviewDate: LocalDate?,
)
