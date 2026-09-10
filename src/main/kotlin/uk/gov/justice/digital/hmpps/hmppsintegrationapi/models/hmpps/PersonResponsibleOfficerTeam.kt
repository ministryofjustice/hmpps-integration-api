package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import com.fasterxml.jackson.annotation.JsonInclude

data class PersonResponsibleOfficerTeam(
  val code: String? = null,
  val description: String? = null,
  val email: String? = null,
  val telephoneNumber: String? = null,
  @JsonInclude(JsonInclude.Include.NON_NULL)
  val provider: PersonResponsibleOfficerTeamProvider? = null,
)
