package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Positive
import java.time.DayOfWeek

@Schema(
  description = """
    Describes time slot and day (or days) the scheduled activity would run. At least one day must be specified.

    e.g. 'AM, Monday, Wednesday and Friday' or 'PM Tuesday, Thursday, Sunday'
  """,
)
data class Exclusion(
  @field:Positive(message = "The week number must be a positive integer")
  @Schema(description = "The week of the schedule this slot relates to", example = "1")
  val weekNumber: Int,
  @field:NotEmpty(message = "The time slot must supplied")
  @Schema(description = "The time slot of the activity schedule", examples = ["AM", "PM", "ED"])
  val timeSlot: String,
  val monday: Boolean,
  val tuesday: Boolean,
  val wednesday: Boolean,
  val thursday: Boolean,
  val friday: Boolean,
  val saturday: Boolean,
  val sunday: Boolean,
  val customStartTime: String?,
  val customEndTime: String?,
  val daysOfWeek: Set<DayOfWeek> =
    setOfNotNull(
      DayOfWeek.MONDAY.takeIf { monday },
      DayOfWeek.TUESDAY.takeIf { tuesday },
      DayOfWeek.WEDNESDAY.takeIf { wednesday },
      DayOfWeek.THURSDAY.takeIf { thursday },
      DayOfWeek.FRIDAY.takeIf { friday },
      DayOfWeek.SATURDAY.takeIf { saturday },
      DayOfWeek.SUNDAY.takeIf { sunday },
    ),
) {
  fun toDaysOfWeek(): Set<DayOfWeek> =
    setOfNotNull(
      DayOfWeek.MONDAY.takeIf { monday },
      DayOfWeek.TUESDAY.takeIf { tuesday },
      DayOfWeek.WEDNESDAY.takeIf { wednesday },
      DayOfWeek.THURSDAY.takeIf { thursday },
      DayOfWeek.FRIDAY.takeIf { friday },
      DayOfWeek.SATURDAY.takeIf { saturday },
      DayOfWeek.SUNDAY.takeIf { sunday },
    )
}
