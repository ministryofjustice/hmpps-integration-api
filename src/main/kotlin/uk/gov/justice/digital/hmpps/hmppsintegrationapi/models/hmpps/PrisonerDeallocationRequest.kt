package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.FutureOrPresent
import jakarta.validation.constraints.NotEmpty
import java.time.LocalDate

@Schema(
  description =
    """
  Describes the deallocation of a prisoner from an activity schedule on a given date in future.  All of the allocations associated with the schedule must be active or suspended but not ended.  """,
)
data class PrisonerDeallocationRequest(
  @Schema(
    description =
      "The prisoner to be deallocated. Must be allocated to the schedule affected by the request.",
  )
  @field:NotEmpty(message = "You must supply a prisoner number on the deallocation request.")
  val prisonerNumber: String,
  @Schema(
    description = "The reason code for the deallocation",
    example = "RELEASED",
    allowableValues = ["COMPLETED", "TRANSFERRED", "WITHDRAWN_STAFF", "WITHDRAWN_OWN", "DISMISSED", "HEALTH", "OTHER", "SECURITY"],
  )
  @field:NotEmpty(message = "The reason code for the deallocation request must supplied.")
  val reasonCode: String?,
  @Schema(
    description =
      "The future date on which this allocation will end. Must not exceed the end date of the allocation, schedule or activity.",
    example = "2023-05-24",
  )
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  @field:FutureOrPresent(message = "End date must not be in the past")
  val endDate: LocalDate,
  @Schema(description = "The scheduled instance id required when de-allocation is a session later today")
  val scheduleInstanceId: Long? = null,
) {
  private fun modelToMap(scheduleId: Long): Map<String, Any?> =
    mapOf(
      "scheduleId" to scheduleId,
      "prisonerNumbers" to listOf(prisonerNumber),
      "reasonCode" to reasonCode,
      "endDate" to endDate,
      "scheduleInstanceId" to scheduleInstanceId,
    )

  fun toHmppsMessage(
    actionedBy: String,
    scheduleId: Long,
  ): HmppsMessage =
    HmppsMessage(
      eventType = HmppsMessageEventType.DEALLOCATE_PRISONER_FROM_ACTIVITY_SCHEDULE,
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
