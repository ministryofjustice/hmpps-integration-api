package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.cpr

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ProbationProvider
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ProbationTeam
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.SupervisionStatus

data class CorePersonRecordSupervisionStatus(
  @Schema(example = "PRISONS", description = "The persons supervision status, either PRISONS, PROBATION, UNKNOWN or NONE")
  val supervisionStatus: SupervisionStatus = SupervisionStatus.UNKNOWN,
  @Schema(example = "A1234AD", description = "The hmppsId of the record from which the supervision status has been determined")
  val determinedBy: String? = null,
  @Schema(description = "The list of prison records returned from CPR")
  val prison: List<PrisonData> = emptyList(),
  @Schema(description = "The list of probation records returned from CPR")
  val probation: List<ProbationData> = emptyList(),
)

data class PrisonData(
  @Schema(example = "A1234AD", description = "The prisoner id (nomis id) on the prisoner record")
  val hmppsId: String,
  @Schema(example = "ACTIVE IN", description = "The prisoners status")
  val prisonerStatus: String? = null,
  @Schema(example = "true", description = "Whether the prisoner is actively in prison")
  val inPrison: Boolean,
  @Schema(example = "MKI", description = "The current prison id")
  val prisonId: String? = null,
  @Schema(example = "MKI", description = "The current prison id or the last i.e. final prison before release. Same as latestLocationId unless latestLocationId was not a prison")
  val lastPrisonId: String? = null,
  @Schema(example = "TAP, CRT, TRN, ADM, REL", description = "Last Movement Type Code of prisoner")
  val lastMovementType: String? = null,
  @Schema(example = "1980-01-01", description = "Date prisoner was received into the prison.")
  val receptionDate: String? = null,
)

data class ProbationData(
  @Schema(example = "A123456", description = "The probation CRN id on the offender record")
  val hmppsId: String,
  @Schema(example = "true", description = "Whether the offender is actively in probation")
  val activeSupervision: Boolean,
  @Schema(description = "The community offender managers provider")
  val provider: ProbationProvider? = null,
  @Schema(description = "The community offender managers team")
  val team: ProbationTeam? = null,
)
