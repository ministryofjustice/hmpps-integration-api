package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class AttendanceHistory(
  @Schema(description = "The internally-generated ID for this attendance", example = "123456")
  val id: Long,
  @Schema(description = "The reason for attending or not")
  val attendanceReason: AttendanceReason,
  @Schema(description = "Free text to allow comments to be put against the attendance", example = "Prisoner was too unwell to attend the activity.")
  val comment: String?,
  @Schema(description = "The date and time the attendance was updated", example = "2023-09-10T09:30:00")
  val recordedTime: String,
  @Schema(description = "The person who updated the attendance", example = "A.JONES")
  val recordedBy: String,
  @Schema(description = "Should payment be issued for SICK, REST or OTHER", example = "true")
  val issuePayment: Boolean?,
  @Schema(description = "Was an incentive level warning issued for REFUSED", example = "true")
  val incentiveLevelWarningIssued: Boolean?,
  @Schema(description = "Free text to allow other reasons for non attendance against the attendance", example = "Prisoner has a valid reason to miss the activity.")
  val otherAbsenceReason: String?,
  @Schema(description = "Free text for any case note entered against the attendance record", example = "Prisoner has refused to attend the activity without a valid reason to miss the activity.")
  val caseNoteText: String?,
)
