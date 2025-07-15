package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class IncidentRoleDto(
  @Schema(description = "The incident role code, If not set then it is assumed they committed the offence on their own", example = "25a")
  val roleCode: String? = null,
  @Schema(description = "The offence rules related to the given incident role, Will not be set of there is no role code")
  val offenceRule: OffenceRuleDetailsDto? = null,
)
