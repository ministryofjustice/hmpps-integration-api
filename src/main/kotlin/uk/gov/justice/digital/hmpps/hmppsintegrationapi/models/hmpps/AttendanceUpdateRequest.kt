package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Positive
import kotlin.collections.List

@Schema(description = "Request object for updating an attendance record")
data class AttendanceUpdateRequest(
  @field:Positive(message = "Attendance ID must be supplied")
  @Schema(description = "The internally-generated ID for this attendance", example = "123456")
  val id: Long,
  @field:NotEmpty(message = "Prison Id")
  @Schema(description = "The prison ID", example = "MDI")
  val prisonId: String,
  @field:NotEmpty(message = "Attendance status")
  @Schema(description = "The status - WAITING, COMPLETED", example = "WAITING")
  val status: String,
  @Schema(description = "The reason codes- SICK, REFUSED, NOT_REQUIRED, REST, CLASH, OTHER, SUSPENDED, CANCELLED, ATTENDED", example = "ATTENDED")
  val attendanceReason: String?,
  @Schema(description = "Comments such as more detail for SICK", example = "Prisoner has COVID-19")
  val comment: String?,
  @Schema(description = "Should payment be issued for SICK, REST or OTHER. Will be ignored if the activity is unpaid.", example = "true")
  val issuePayment: Boolean?,
  @Schema(description = "Was an incentive level warning issued for REFUSED", example = "true")
  val incentiveLevelWarningIssued: Boolean?,
  @Schema(description = "The absence reason for OTHER", example = "Prisoner has another reason for missing the activity")
  val otherAbsenceReason: String?,
) {
  fun modelToMap(): Map<String, Any?> =
    mapOf(
      "id" to this.id,
      "prisonCode" to this.prisonId,
      "status" to this.status,
      "attendanceReason" to this.attendanceReason,
      "comment" to this.comment,
      "issuePayment" to this.issuePayment,
      "caseNote" to null,
      "incentiveLevelWarningIssued" to this.incentiveLevelWarningIssued,
      "otherAbsenceReason" to this.otherAbsenceReason,
    )
}

fun List<AttendanceUpdateRequest>.toHmppsMessage(actionedBy: String): HmppsMessage =
  HmppsMessage(
    eventType = HmppsMessageEventType.MARK_PRISONER_ATTENDANCE,
    messageAttributes =
      mapOf(
        "attendanceUpdateRequests" to this.map { it.modelToMap() },
      ),
    who = actionedBy,
  )

fun List<AttendanceUpdateRequest>.toTestMessage(actionedBy: String?): HmppsMessage =
  HmppsMessage(
    eventType = HmppsMessageEventType.TEST_EVENT,
    messageAttributes = emptyMap(),
    who = actionedBy,
  )
