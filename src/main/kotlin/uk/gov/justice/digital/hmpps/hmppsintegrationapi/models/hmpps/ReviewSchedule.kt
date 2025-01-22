package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.USE_DEFAULTS)
data class ReviewSchedules(
  @Schema(
    description = """
       A list of review schedules for this prisoner.
       Note that this will return multiple versions of the the same schedule.
       This is because down stream clients need to know the previous states of the review schedules.
       grouping by reference and then ordering by highest version number will present the review schedules with the most up to date data.
    """,
  )
  val reviewSchedules: List<ReviewSchedule>,
)

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.USE_DEFAULTS)
data class ReviewSchedule(
  @Schema(
    description = "The Nomis number of the person.",
    example = "A1234BC",
  )
  val nomisNumber: String? = null,
  @Schema(
    description = "The UUID reference for the review schedule record.",
  )
  val reference: UUID,
  @Schema(
    description = "The earliest date that the review should be carried out.",
  )
  val reviewDateFrom: LocalDate,
  @Schema(
    description = "The latest date that the review should be carried out.",
  )
  val reviewDateTo: LocalDate,
  @Schema(
    description = """
      The status of this review schedule possible values are:
        SCHEDULED
        EXEMPT_PRISONER_DRUG_OR_ALCOHOL_DEPENDENCY
        EXEMPT_PRISONER_OTHER_HEALTH_ISSUES
        EXEMPT_PRISONER_FAILED_TO_ENGAGE
        EXEMPT_PRISONER_ESCAPED_OR_ABSCONDED
        EXEMPT_PRISONER_SAFETY_ISSUES
        EXEMPT_PRISON_REGIME_CIRCUMSTANCES
        EXEMPT_PRISON_STAFF_REDEPLOYMENT
        EXEMPT_PRISON_OPERATION_OR_SECURITY_ISSUE
        EXEMPT_SECURITY_ISSUE_RISK_TO_STAFF
        EXEMPT_SYSTEM_TECHNICAL_ISSUE
        EXEMPT_PRISONER_TRANSFER
        EXEMPT_PRISONER_RELEASE
        EXEMPT_PRISONER_DEATH
        EXEMPT_UNKNOWN
        COMPLETED
      """,
  )
  val status: String,
  @Schema(
    description = """
      The calculation rule that was used to workout the review schedule window.
      Possible values are:
          PRISONER_READMISSION
          PRISONER_TRANSFER
          BETWEEN_RELEASE_AND_3_MONTHS_TO_SERVE
          BETWEEN_3_MONTHS_AND_3_MONTHS_7_DAYS_TO_SERVE
          BETWEEN_3_MONTHS_8_DAYS_AND_6_MONTHS_TO_SERVE
          BETWEEN_6_AND_12_MONTHS_TO_SERVE
          BETWEEN_12_AND_60_MONTHS_TO_SERVE
          MORE_THAN_60_MONTHS_TO_SERVE
          INDETERMINATE_SENTENCE
          PRISONER_ON_REMAND
          PRISONER_UN_SENTENCED
      """,
  )
  val calculationRule: String,
  val createdBy: String,
  val createdByDisplayName: String,
  val createdAt: Instant,
  val createdAtPrison: String,
  val updatedBy: String,
  val updatedByDisplayName: String,
  val updatedAt: Instant,
  val updatedAtPrison: String,
  @Schema(
    description = """
      The review schedule can change status numerous times.
      The version number indicates which version of the review schedule this one is, the higher the number the newer the update.
      use the version number in conjunction with the reference UUID.
      ie if you group the schedules by reference then the version numbers represent different versions of the same schedule.
      The final status of each grouping is COMPLETED.
    """,
  )
  val version: Int,
  @Schema(
    description = "When the review schedule has a completed review this will be populated by the name of the person who did the review.",
  )
  var reviewCompletedBy: String?,
  @Schema(
    description = "When the review schedule has a completed review this will be populated by the role of the person who did the review.",
  )
  var reviewCompletedByRole: String?,
  @Schema(
    description = "When the review schedule has a completed review this will be populated with the date of the review.",
  )
  var reviewCompletedAt: Instant?,
  @Schema(
    description = """
      When the review schedule has a completed review this will be populated with the type of the review.
      values are:
        REGULAR,
        PRE_RELEASE
    """,
  )
  val reviewType: String?,
  @Schema(
    description = """
      When the review schedule has a completed review this will be populated with the reason for the review.
      values are:
        REOFFENCE,
        TRANSFER,
        REGULAR
    """,
  )
  val reviewReason: String?,
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
