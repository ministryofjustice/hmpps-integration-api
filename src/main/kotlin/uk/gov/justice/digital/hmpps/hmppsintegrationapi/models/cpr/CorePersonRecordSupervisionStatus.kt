package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.cpr

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ProbationProvider
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ProbationTeam
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.SupervisionStatus

data class CorePersonRecordSupervisionStatus(
  val supervisionStatus: SupervisionStatus = SupervisionStatus.UNKNOWN,
  val determinedBy: String? = null,
  val prison: List<PrisonData> = emptyList(),
  val probation: List<ProbationData> = emptyList(),
)

data class PrisonData(
  val hmppsId: String,
  val prisonerStatus: String? = null,
  val inPrison: Boolean,
  val prisonId: String? = null,
  val lastPrisonId: String? = null,
  val lastMovementType: String? = null,
  val receptionDate: String? = null,
)

data class ProbationData(
  val hmppsId: String,
  val activeSupervision: Boolean,
  val provider: ProbationProvider? = null,
  val team: ProbationTeam? = null,
)
