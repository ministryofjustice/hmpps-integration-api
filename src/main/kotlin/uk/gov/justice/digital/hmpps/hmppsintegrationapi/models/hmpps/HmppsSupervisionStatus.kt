package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.SupervisionStatus

@JsonInclude(JsonInclude.Include.NON_NULL)
data class HmppsSupervisionStatus(
  @Schema(description = "The offenders supervision status (PRISONS, PROBATION, UNKNOWN or NONE)", example = "PRISONS")
  val supervisionStatus: SupervisionStatus,
  @Schema(description = "The offenders prison code if in prison", example = "MKI")
  val prisonId: String? = null,
  @Schema(description = "The offenders probation provider")
  val probationProvider: ProbationProvider? = null,
)

data class ProbationProvider(
  @Schema(description = "The probation provider code", example = "N07")
  val code: String? = null,
  @Schema(description = "The probation provider description", example = "London")
  val description: String? = null,
)
