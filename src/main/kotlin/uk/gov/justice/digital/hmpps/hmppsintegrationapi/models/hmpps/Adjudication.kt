package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class Adjudication(
  @Schema(description = "Incident details")
  val incidentDetails: IncidentDetailsDto? = null,
  @Schema(description = "Is classified as a youth offender", example = "false")
  val isYouthOffender: Boolean? = null,
  @Schema(description = "Information about the role of this prisoner in the incident")
  val incidentRole: IncidentRoleDto? = null,
  @Schema(description = "Details about the offence the prisoner is accused of")
  val offenceDetails: OffenceDto? = null,
  @Schema(description = "The status of the reported adjudication", example = "ACCEPTED")
  val status: String? = null,
  @Schema(description = "The reason for the status of the reported adjudication")
  val statusReason: String? = null,
  @Schema(description = "The name for the status of the reported adjudication")
  val statusDetails: String? = null,
  @Schema(description = "Hearings related to adjudication")
  val hearings: List<HearingDto>? = null,
  @Schema(description = "Hearings, hearing outcomes, referrals and outcomes in chronological order")
  val outcomes: List<OutcomeHistoryDto>? = null,
  @Schema(description = "Punishments")
  val punishments: List<PunishmentDto>? = null,
  @Schema(description = "Punishment comments")
  val punishmentComments: List<PunishmentCommentDto>? = null,
)
