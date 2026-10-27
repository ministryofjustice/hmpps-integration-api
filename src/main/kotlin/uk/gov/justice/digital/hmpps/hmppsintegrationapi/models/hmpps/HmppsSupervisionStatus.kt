package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import com.fasterxml.jackson.annotation.JsonInclude
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.SupervisionStatus

@JsonInclude(JsonInclude.Include.NON_NULL)
data class HmppsSupervisionStatus(
  val supervisionStatus: SupervisionStatus,
  val prisonId: String? = null,
  val probationProvider: ProbationProvider? = null,
)

data class ProbationProvider(
  val code: String? = null,
  val description: String? = null,
)
