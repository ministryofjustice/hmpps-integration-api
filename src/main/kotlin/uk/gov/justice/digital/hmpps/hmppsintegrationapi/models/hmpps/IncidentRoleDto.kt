package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class IncidentRoleDto(
  val roleCode: String? = null,
  val offenceRule: OffenceRuleDetailsDto? = null,
)
