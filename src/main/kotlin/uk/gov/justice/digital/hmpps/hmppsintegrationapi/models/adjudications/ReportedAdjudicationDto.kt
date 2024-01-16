package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.adjudications

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Adjudication

data class ReportedAdjudicationDto(
  val incidentDetails: IncidentDetailsDto,
) {
  fun toAdjudication(): Adjudication = Adjudication(incidentDetails = uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.IncidentDetailsDto(dateTimeOfIncident = this.incidentDetails.dateTimeOfIncident))
}

data class IncidentDetailsDto(
  val locationId: Int? = null,
  val dateTimeOfIncident: String? = null,
  val dateTimeOfDiscovery: String? = null,
  val handoverDeadline: String? = null,
)

data class IncidentRoleDto(
  val roleCode: String? = null,
  val offenceRule: OffenceRuleDetailsDto? = null,
  val dateTimeOfDiscovery: String? = null,
  val handoverDeadline: String? = null,
)

data class OffenceRuleDetailsDto(
  val paragraphNumber: String? = null,
  val paragraphDescription: String? = null,
)

data class OffenceDto(
  val offenceCode: Int? = null,
  val offenceRule: OffenceRuleDto? = null,
  val victimPrisonersNumber: String? = null,
  val victimsStaffUsername: String? = null,
  val victimOtherPersonsName: String? = null,

)

data class OffenceRuleDto(
  val paragraphNumber: String? = null,
  val paragraphDescription: String? = null,
  val nomisCode: String? = null,
  val withOthersNomisCode: String? = null,
)

data class IncidentStatementDto(
  val statement: String? = null,
  val completed: Boolean? = null,
)

data class ReportedDamageDto(
  val code: String? = null,
  val details: String? = null,
  val reporter: String? = null,
)

data class ReportedEvidenceDto(
  val code: String? = null,
  val identifier: String? = null,
  val details: String? = null,
  val reporter: String? = null,
)

data class ReportedWitnessDto(
  val code: String? = null,
  val firstName: String? = null,
  val lastName: String? = null,
  val reporter: String? = null,
)

data class HearingDto(
  val id: Int? = null,
  val locationId: Int? = null,
  val dateTimeOfHearing: String? = null,
  val oicHearingType: String? = null,
  val outcome: HearingOutcomeDto? = null,
  val agencyId: String? = null,
)

data class HearingOutcomeDto(
  val id: Int? = null,
  val adjudicator: String? = null,
  val code: String? = null,
  val reason: String? = null,
  val details: String? = null,
  val plea: String? = null,
)

data class DisIssueHistoryDto(
  val issuingOfficer: String? = null,
  val dateTimeOfIssue: String? = null,
)

data class OutcomeHistoryDto(
  val hearing: HearingDto? = null,
  val outcome: CombinedOutcomeDto? = null,
)

data class CombinedOutcomeDto(
  val outcome: OutcomeDto? = null,
  val referralOutcome: OutcomeDto? = null,
)

data class OutcomeDto(
  val id: Int? = null,
  val code: String? = null,
  val details: String? = null,
  val reason: String? = null,
  val quashedReason: String? = null,
  val canRemove: Boolean? = null,
)

data class PunishmentDto(
  val id: Int? = null,
  val type: String? = null,
  val privilegeType: String? = null,
  val otherPrivilege: String? = null,
  val stoppagePercentage: Int? = null,
  val activatedBy: String? = null,
  val activatedFrom: String? = null,
  val schedule: PunishmentScheduleDto? = null,
  val consecutiveChargeNumber: String? = null,
  val consecutiveReportAvailable: Boolean? = null,
  val damagesOwedAmount: Int? = null,
  val canRemove: Boolean? = null,
)

data class PunishmentScheduleDto(
  val days: Int? = null,
  val startData: String? = null,
  val endDate: String? = null,
  val suspendedUntil: String? = null,
)

data class PunishmentCommentDto(
  val id: Int? = null,
  val comment: String? = null,
  val reasonForChange: String? = null,
  val createdByUserId: String? = null,
  val dateTime: String? = null,
)