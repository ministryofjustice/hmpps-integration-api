package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.remandAndSentencing.CourtOutComeType
import java.time.LocalDate

data class CourtCasesSummary(
  @Schema(description = "The date of first conviction", example = "2022-10-01")
  val dateOfFirstConviction: LocalDate? = null,
  @Schema(description = "The outcome of the last court appearance")
  val courtOutcome: CourtOutcome? = null,
  @Schema(description = "The court code of latest sentenced outcome", example = "ACCRYC")
  val courtCode: String? = null,
  val allAppearances: List<Appearance> = emptyList(),
)

data class Appearance(
  @Schema(description = "The date of the appearance", example = "2022-10-01")
  val dateOfAppearance: LocalDate? = null,
  @Schema(description = "The outcome of the court appearance")
  val courtOutcome: CourtOutcome? = null,
  @Schema(description = "The court code of the court appearance", example = "ACCRYC")
  val courtCode: String? = null,
)

data class CourtOutcome(
  @Schema(description = "The outcome type", example = "SENTENCING")
  val outcomeType: CourtOutComeType,
  @Schema(description = "The outcome name", example = "Imprisonment")
  val outcomeName: String? = null,
)
