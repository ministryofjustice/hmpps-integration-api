package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.time.LocalTime

@Schema(description = "Describes a scheduled activity for a prisoner")
data class ActivityScheduledInstanceForPrisoner(
  @Schema(description = "Unique ID of the scheduled instance", example = "123456")
  val id: Long,
  @Schema(description = "The id of the activity", example = "1")
  val activityId: Int,
  @Schema(description = "Date of the scheduled instance", example = "2022-10-20")
  @JsonFormat(pattern = "yyyy-MM-dd")
  val sessionDate: LocalDate,
  @Schema(description = "Scheduled start time", example = "09:00")
  @JsonFormat(pattern = "HH:mm")
  val startTime: LocalTime? = null,
  @Schema(description = "Scheduled end time", example = "12:00")
  @JsonFormat(pattern = "HH:mm")
  val endTime: LocalTime? = null,
  @Schema(description = "Set to true if this event will take place in the prisoner's cell", example = "false")
  val inCell: Boolean,
  @Schema(description = "Flag to indicate if the location of the activity is on wing", example = "false")
  val onWing: Boolean,
  @Schema(description = "Flag to indicate if the location of the activity is off wing and not in a listed location", example = "false")
  val offWing: Boolean,
  val scheduleDescription: String? = null,
  @Schema(description = "The category for this activity, one of the high-level categories")
  val activityCategory: String,
  @Schema(description = "The title of the activity for this attendance record", example = "Math Level 1")
  val activitySummary: String? = null,
  @Schema(description = "Set to true if this event has been cancelled", example = "false")
  val cancelled: Boolean = false,
  @Schema(description = "Set to true if this prisoner is suspended from the scheduled event", example = "false")
  val suspended: Boolean = false,
  @Schema(description = "Set to true if this prisoner is auto-suspended from the scheduled event", example = "false")
  val autoSuspended: Boolean = false,
  @Schema(description = "Time slot of scheduled instance", example = "AM")
  val timeSlot: String,
  @Schema(description = "Should activity payment be issued for SICK, REST or OTHER", example = "true")
  val issuePayment: Boolean? = false,
  @Schema(description = "The activity attendance status - WAITING or COMPLETED", example = "WAITING")
  val attendanceStatus: String? = null,
  @Schema(description = "The code for the activity (non) attendance reason", example = "SICK")
  val attendanceReasonCode: String? = null,
  @Schema(description = "Set to true if this activity is a paid activity", example = "false")
  val paidActivity: Boolean,
  val possibleAdvanceAttendance: Boolean,
)
