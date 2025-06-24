package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class AdvanceAttendance(
  @Schema(description = "Unique ID of the advance attendance record", example = "123456")
  val id: Long,
  @Schema(description = "ID of the schedule instance", example = "654321")
  val scheduleInstanceId: Long,
  @Schema(description = "Prison number for the offender", example = "A1234AA")
  val prisonerNumber: String,
  @Schema(description = "Should payment be issued for this attendance", example = "true")
  val issuePayment: Boolean? = null,
  @Schema(description = "Amount to be paid in pence", example = "100")
  val payAmount: Int? = null,
  @Schema(description = "Date and time the attendance was recorded", example = "2023-09-10T09:30:00")
  val recordedTime: String?,
  @Schema(description = "Username of the person who recorded the attendance", example = "A.JONES")
  val recordedBy: String?,
  @Schema(description = "History of changes to this advance attendance record")
  val attendanceHistory: List<AdvanceAttendanceHistory>?,
)
