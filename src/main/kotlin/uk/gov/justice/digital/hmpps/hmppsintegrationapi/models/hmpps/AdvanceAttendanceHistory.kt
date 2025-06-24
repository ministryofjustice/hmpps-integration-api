package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class AdvanceAttendanceHistory(
  @Schema(description = "Unique ID of the advance attendance history record", example = "789123")
  val id: Long,
  @Schema(description = "Indicates if payment was issued at the time", example = "true")
  val issuePayment: Boolean,
  @Schema(description = "Timestamp when this history entry was recorded", example = "2023-09-10T09:30:00")
  val recordedTime: String,
  @Schema(description = "Username of the person who recorded the entry", example = "A.JONES")
  val recordedBy: String,
)
