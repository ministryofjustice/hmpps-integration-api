package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.USE_DEFAULTS)
data class ReviewSchedules(
  val reviewSchedules: List<ReviewSchedule>,
)

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.USE_DEFAULTS)
data class ReviewSchedule(
  val reference: UUID,
  val reviewDateFrom: LocalDate,
  val reviewDateTo: LocalDate,
  val status: String,
  val calculationRule: String,
  val createdBy: String,
  val createdByDisplayName: String,
  val createdAt: Instant,
  val createdAtPrison: String,
  val updatedBy: String,
  val updatedByDisplayName: String,
  val updatedAt: Instant,
  val updatedAtPrison: String,
  val version: Int,
  var reviewCompletedBy: String?,
  var reviewCompletedByRole: String?,
  var reviewCompletedAt: Instant?,
  val reviewType: String?,
  val reviewReason: String?
)

data class ActionPlanReviewsResponse(
  val latestReviewSchedule: ScheduledActionPlanReviewResponse? = null,
  val completedReviews: List<CompletedActionPlanReviewResponse>,
)

data class ScheduledActionPlanReviewResponse(
  val reviewDateFrom: LocalDate,
  val reviewDateTo: LocalDate,
  val status: String,
  val calculationRule: String,
  val createdBy: String,
  val createdByDisplayName: String,
  val createdAt: Instant,
  val createdAtPrison: String,
  val updatedBy: String,
  val updatedByDisplayName: String,
  val updatedAt: Instant,
  val updatedAtPrison: String,
  val exemptionReason: String? = null,
  val version: Int? = null,
)

data class CompletedActionPlanReviewResponse(
  val reference: UUID,
  val deadlineDate: LocalDate,
  val completedDate: LocalDate,
  val createdBy: String,
  val createdByDisplayName: String,
  val createdAt: java.time.OffsetDateTime,
  val createdAtPrison: String,
  val reviewScheduleReference: UUID? = null,
  val conductedBy: String? = null,
  val conductedByRole: String? = null,
  val updatedBy: String,
  val updatedByDisplayName: String,
  val updatedAt: Instant,
  val preRelease: Boolean,
)
