package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

data class PrisonerBaseLocation(
  @Schema(description = "In prison or not")
  val inPrison: Boolean? = null,
  @Schema(description = "Prison ID", example = "MDI")
  val prisonId: String? = null,
  @Schema(description = "The last prison for the prisoner (which is the same as the prisonId if they are still inside prison)", example = "MDI")
  val lastPrisonId: String? = null,
  @Schema(description = "Last movement type", example = "Admission")
  val lastMovementType: LastMovementType? = null,
  @Schema(description = "Reception date", example = "2025-04-01")
  val receptionDate: LocalDate? = null,
)

@Schema
enum class LastMovementType(
  val value: String,
) {
  @JsonProperty("Admission")
  ADMISSION("Admission"),

  @JsonProperty("Release")
  RELEASE("Release"),

  @JsonProperty("Transfers")
  TRANSFERS("Transfers"),

  @JsonProperty("Court")
  COURT("Court"),

  @JsonProperty("Temporary Absence")
  TEMPORARY_ABSENCE("Temporary Absence"),
}
