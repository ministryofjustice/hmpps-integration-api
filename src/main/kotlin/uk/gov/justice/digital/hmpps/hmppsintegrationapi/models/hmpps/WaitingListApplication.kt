package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.time.LocalDateTime

@Schema(description = "Represents a prisoner's application to be on a waiting list for an activity.")
data class WaitingListApplication(
  @Schema(description = "The unique identifier for the waiting list application.", example = "12345")
  val id: Long,
  @Schema(description = "The identifier for the activity.", example = "67890")
  val activityId: Long,
  @Schema(description = "The identifier for the activity schedule.", example = "54321")
  val scheduleId: Long,
  @Schema(description = "The identifier for the allocation, if the application resulted in one.", example = "98765")
  val allocationId: Long? = null,
  @Schema(description = "The prison identifier.", example = "MDI")
  val prisonId: String,
  @Schema(description = "The prisoner's number.", example = "A1234BC")
  val prisonerNumber: String,
  @Schema(description = "The prisoner's booking identifier.", example = "112233")
  val bookingId: Long,
  @Schema(description = "The status of the application.", example = "PENDING")
  val status: String,
  @Schema(description = "The timestamp when the status was last updated.", example = "2023-02-28T14:30:00")
  val statusUpdatedTime: LocalDateTime? = null,
  @Schema(description = "The date for which the application was requested.", example = "2023-03-01")
  val requestedDate: LocalDate,
  @Schema(description = "Comments relating to the application.", example = "Prisoner is keen to start.")
  val comments: String? = null,
  @Schema(description = "The reason the application was declined.", example = "Not suitable for this activity.")
  val declinedReason: String? = null,
  @Schema(description = "The timestamp when the application was created.", example = "2023-02-27T10:00:00")
  val creationTime: LocalDateTime,
  @Schema(description = "The timestamp when the application was last updated.", example = "2023-02-28T14:30:00")
  val updatedTime: LocalDateTime? = null,
  @Schema(description = "The prisoner's earliest release date information.")
  val earliestReleaseDate: EarliestReleaseDate,
  @Schema(description = "Indicates if the prisoner has any non-associations.", example = "true")
  val nonAssociations: Boolean? = null,
)
