package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.adjudications

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Adjudication
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HearingDto
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HearingOutcomeDto
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.IncidentDetailsDto
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.IncidentRoleDto
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.OffenceDto
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.OffenceRuleDetailsDto
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.OffenceRuleDto

data class ReportedAdjudication(
  val incidentDetails: AdjudicationsIncidentDetails,
  val isYouthOffender: Boolean,
  val incidentRole: AdjudicationsIncidentRole? = null,
  val offenceDetails: AdjudicationsOffenceDetails? = null,
  val hearings: List<AdjudicationsHearing?> = emptyList(),
) {
  fun toAdjudication(): Adjudication =
    Adjudication(
      incidentDetails = IncidentDetailsDto(
        locationId = this.incidentDetails.locationId,
        dateTimeOfIncident = this.incidentDetails.dateTimeOfIncident,
        dateTimeOfDiscovery = this.incidentDetails.dateTimeOfDiscovery,
        handoverDeadline = this.incidentDetails.handoverDeadline,
      ),
      isYouthOffender = this.isYouthOffender,
      incidentRole = IncidentRoleDto(
        roleCode = this.incidentRole?.roleCode,
        offenceRule = OffenceRuleDetailsDto(
          paragraphNumber = this.incidentRole?.offenceRule?.paragraphNumber,
          paragraphDescription = this.incidentRole?.offenceRule?.paragraphDescription,
        ),
        dateTimeOfDiscovery = this.incidentRole?.dateTimeOfDiscovery,
        handoverDeadline = this.incidentRole?.handoverDeadline,
      ),
      offenceDetails = OffenceDto(
        offenceCode = this.offenceDetails?.offenceCode,
        offenceRule = OffenceRuleDto(
          paragraphNumber = this.offenceDetails?.offenceRule?.paragraphNumber,
          paragraphDescription = this.offenceDetails?.offenceRule?.paragraphDescription,
          nomisCode = this.offenceDetails?.offenceRule?.nomisCode,
          withOthersNomisCode = this.offenceDetails?.offenceRule?.withOthersNomisCode,

        ),
        victimPrisonersNumber = this.offenceDetails?.victimPrisonersNumber,
        victimsStaffUsername = this.offenceDetails?.victimsStaffUsername,
        victimOtherPersonsName = this.offenceDetails?.victimOtherPersonsName,
      ),
      hearings = this.hearings.map {
        HearingDto(
          id = it?.id,
          locationId = it?.locationId,
          dateTimeOfHearing = it?.dateTimeOfHearing,
          oicHearingType = it?.oicHearingType,
          outcome = HearingOutcomeDto(
            id = it?.outcome?.id,
            adjudicator = it?.outcome?.adjudicator,
            code = it?.outcome?.code,
            reason = it?.outcome?.reason,
            details = it?.outcome?.details,
            plea = it?.outcome?.plea,
          ),
          agencyId = it?.agencyId,
        )
      },
    )
}

data class OutcomeHistoryDto(
  val hearing: HearingDto? = null,
  val outcome: CombinedOutcomeDto? = null,
)

data class CombinedOutcomeDto(
  val outcome: OutcomeDto? = null,
  val referralOutcome: OutcomeDto? = null,
)

data class OutcomeDto(
  val id: Number? = null,
  val code: String? = null,
  val details: String? = null,
  val reason: String? = null,
  val quashedReason: String? = null,
  val canRemove: Boolean? = null,
)

data class PunishmentDto(
  val id: Number? = null,
  val type: String? = null,
  val privilegeType: String? = null,
  val otherPrivilege: String? = null,
  val stoppagePercentage: Number? = null,
  val activatedBy: String? = null,
  val activatedFrom: String? = null,
  val schedule: PunishmentScheduleDto? = null,
  val consecutiveChargeNumber: String? = null,
  val consecutiveReportAvailable: Boolean? = null,
  val damagesOwedAmount: Number? = null,
  val canRemove: Boolean? = null,
)

data class PunishmentScheduleDto(
  val days: Number? = null,
  val startData: String? = null,
  val endDate: String? = null,
  val suspendedUntil: String? = null,
)

data class PunishmentCommentDto(
  val id: Number? = null,
  val comment: String? = null,
  val reasonForChange: String? = null,
  val createdByUserId: String? = null,
  val dateTime: String? = null,
)
