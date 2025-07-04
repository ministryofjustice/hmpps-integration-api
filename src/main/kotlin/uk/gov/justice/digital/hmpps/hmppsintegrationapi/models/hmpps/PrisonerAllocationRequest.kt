package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.FutureOrPresent
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDate

@Schema(
  description =
    """
  Describes the allocation of a prisoner to an activity schedule on a given date in future.  All allocations must have a valid scheduled instance if starting today, include or exclude a pay band based on payment status, fall within the activity’s date range, and ensure the prisoner is not already allocated or on conflicting waiting lists.
  """,
)
data class PrisonerAllocationRequest(
  @Schema(description = "The prisoner number (Nomis ID)", example = "A1234AA")
  @field:NotBlank(message = "Prisoner number must be supplied")
  @field:Size(max = 7, message = "Prisoner number cannot be more than 7 characters")
  val prisonerNumber: String,
  @Schema(
    description = "Where a prison uses pay bands to differentiate earnings, this is the pay band code given to this prisoner. Can be null for unpaid activities.",
    example = "1",
  )
  val payBandId: Long? = null,
  @Schema(description = "The date when the prisoner will start the activity", example = "2022-09-10")
  @JsonFormat(pattern = "yyyy-MM-dd")
  @field:NotNull(message = "Start date must be supplied")
  @field:FutureOrPresent(message = "Start date must not be in the past")
  val startDate: LocalDate,
  @Schema(description = "The date when the prisoner will stop attending the activity", example = "2023-09-10")
  @JsonFormat(pattern = "yyyy-MM-dd")
  val endDate: LocalDate? = null,
  @Schema(description = "The days and times that the prisoner is excluded from this activity's schedule")
  val exclusions: List<Exclusion>? = null,
  @Schema(description = "The scheduled instance id required when allocation starts today")
  val scheduleInstanceId: Long? = null,
  @Schema(description = "For internal use only")
  val testEvent: String? = null,
) {
  private fun modelToMap(scheduleId: Long): Map<String, Any?> =
    mapOf(
      "scheduleId" to scheduleId,
      "prisonerNumber" to prisonerNumber,
      "payBandId" to payBandId,
      "startDate" to startDate,
      "endDate" to endDate,
      "exclusions" to
        exclusions?.map {
          mapOf(
            "timeSlot" to it.timeSlot,
            "weekNumber" to it.weekNumber,
            "monday" to it.monday,
            "tuesday" to it.tuesday,
            "wednesday" to it.wednesday,
            "thursday" to it.thursday,
            "friday" to it.friday,
            "saturday" to it.saturday,
            "sunday" to it.sunday,
            "customStartTime" to it.customStartTime,
            "customEndTime" to it.customEndTime,
            "daysOfWeek" to it.toDaysOfWeek(),
          )
        },
      "scheduleInstanceId" to scheduleInstanceId,
    )

  fun toHmppsMessage(
    actionedBy: String,
    scheduleId: Long,
  ): HmppsMessage =
    HmppsMessage(
      eventType = HmppsMessageEventType.ALLOCATE_PRISONER_TO_ACTIVITY_SCHEDULE,
      messageAttributes = this.modelToMap(scheduleId),
      who = actionedBy,
    )

  fun toTestMessage(actionedBy: String?): HmppsMessage =
    HmppsMessage(
      eventType = HmppsMessageEventType.TEST_EVENT,
      messageAttributes = emptyMap(),
      who = actionedBy,
    )
}
