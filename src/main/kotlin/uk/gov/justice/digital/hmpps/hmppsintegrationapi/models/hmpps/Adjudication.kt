package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class Adjudication(
  val incidentDetails: IncidentDetailsDto? = null,
  val isYouthOffender: Boolean? = null,
  val incidentRole: IncidentRoleDto? = null,
  val offenceDetails: OffenceDto? = null,
  val hearings: List<HearingDto>? = null,
  val outcomes: List<OutcomeHistoryDto>? = null,
)

// data class PunishmentDto(
//  val id: Number? = null,
//  val type: String? = null,
//  val privilegeType: String? = null,
//  val otherPrivilege: String? = null,
//  val stoppagePercentage: Number? = null,
//  val activatedBy: String? = null,
//  val activatedFrom: String? = null,
//  val schedule: PunishmentScheduleDto? = null,
//  val consecutiveChargeNumber: String? = null,
//  val consecutiveReportAvailable: Boolean? = null,
//  val damagesOwedAmount: Number? = null,
//  val canRemove: Boolean? = null,
// )
//
// data class PunishmentScheduleDto(
//  val days: Number? = null,
//  val startData: String? = null,
//  val endDate: String? = null,
//  val suspendedUntil: String? = null,
// )
//
// data class PunishmentCommentDto(
//  val id: Number? = null,
//  val comment: String? = null,
//  val reasonForChange: String? = null,
//  val createdByUserId: String? = null,
//  val dateTime: String? = null,
// )
