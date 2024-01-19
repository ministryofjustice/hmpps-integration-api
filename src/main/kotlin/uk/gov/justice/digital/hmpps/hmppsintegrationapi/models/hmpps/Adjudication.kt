package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class Adjudication(
  val incidentDetails: IncidentDetailsDto? = null,
  val isYouthOffender: Boolean? = null,
  val incidentRole: IncidentRoleDto? = null,
  val offenceDetails: OffenceDto? = null,
  val hearings: List<HearingDto>? = null,
  val outcomes: List<OutcomeHistoryDto>? = null,
  val punishments: List<PunishmentDto>? = null,
  val punishmentComments: List<PunishmentCommentDto>? = null,
)
