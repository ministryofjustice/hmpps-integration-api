package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class ActivityScheduledInstanceForPrisoner(
  @Schema(description = "Unique ID of the scheduled instance", example = "123456")
  val id: Long,
  @Schema(description = "Date of the scheduled instance", example = "2022-10-20")
  val date: String,
  @Schema(description = "Scheduled start time", example = "09:00")
  val startTime: String,
  @Schema(description = "Scheduled end time", example = "12:00")
  val endTime: String,
  @Schema(description = "Time slot for the activity", example = "AM")
  val timeSlot: String,
  @Schema(description = "Was the session cancelled?", example = "false")
  val cancelled: Boolean,
  @Schema(description = "Time the session was cancelled", example = "2022-10-19T15:30:00")
  val cancelledTime: String? = null,
  @Schema(description = "Name of the person who cancelled the session", example = "Adam Smith")
  val cancelledBy: String? = null,
  @Schema(description = "Reason the session was cancelled", example = "Staff unavailable")
  val cancelledReason: String? = null,
  @Schema(description = "Was payment still issued despite cancellation?", example = "true")
  val cancelledIssuePayment: Boolean? = null,
  @Schema(description = "Optional comment on the scheduled session", example = "Teacher unavailable")
  val comment: String? = null,
  @Schema(description = "ID of the previous scheduled instance", example = "123455")
  val previousScheduledInstanceId: Long? = null,
  @Schema(description = "Date of the previous scheduled instance", example = "2022-10-19")
  val previousScheduledInstanceDate: String? = null,
  @Schema(description = "ID of the next scheduled instance", example = "123457")
  val nextScheduledInstanceId: Long? = null,
  @Schema(description = "Date of the next scheduled instance", example = "2022-10-21")
  val nextScheduledInstanceDate: String? = null,
  @Schema(description = "Attendances recorded for this session")
  val attendances: List<Attendance>,
  @Schema(description = "Advance attendance history recorded for this session")
  val advanceAttendances: List<AdvanceAttendance>,
  @Schema(description = "Details of the associated activity schedule")
  val activitySchedule: ActivitySchedule,
)
