package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.annotation.Nullable
import jakarta.validation.Valid
import jakarta.validation.constraints.FutureOrPresent
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
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
  @NotNull(message = "The end date for the deallocation request must supplied.")
  val endDate: LocalDate?,
  @Schema(description = "Describes a case note to be added to the prisoner's profile as part of the deallocation.")
  @field:Valid
  @field:Nullable
  val caseNote: AddCaseNoteRequest? = null,
  @Schema(description = "The scheduled instance id required when de-allocation is a session later today")
  val scheduleInstanceId: Long? = null,
)
