package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities

import java.time.LocalDateTime

data class ActivitiesActivity(
  val id: Long,
  val prisonCode: String,
  val attendanceRequired: Boolean,
  val inCell: Boolean,
  val onWing: Boolean,
  val offWing: Boolean,
  val pieceWork: Boolean,
  val outsideWork: Boolean,
  val payPerSession: String,
  val summary: String,
  val description: String?,
  val category: ActivitiesActivityCategory,
  val riskLevel: String,
  val minimumEducationLevel: List<ActivitiesMinimumEducationLevel>,
  val endDate: String?,
  val capacity: Int,
  val allocated: Int,
  val createdTime: LocalDateTime,
  val activityState: String,
  val paid: Boolean,
)
