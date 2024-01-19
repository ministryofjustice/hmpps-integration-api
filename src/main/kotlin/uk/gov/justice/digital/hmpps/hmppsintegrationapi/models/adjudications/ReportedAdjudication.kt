package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.adjudications

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Adjudication
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CombinedOutcomeDto
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HearingDto
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HearingOutcomeDto
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.IncidentDetailsDto
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.IncidentRoleDto
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.OffenceDto
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.OffenceRuleDetailsDto
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.OffenceRuleDto
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.OutcomeDto
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.OutcomeHistoryDto
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PunishmentCommentDto
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PunishmentDto
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PunishmentScheduleDto

data class ReportedAdjudication(
  val incidentDetails: AdjudicationsIncidentDetails,
  val isYouthOffender: Boolean,
  val incidentRole: AdjudicationsIncidentRole? = null,
  val offenceDetails: AdjudicationsOffenceDetails? = null,
  val hearings: List<AdjudicationsHearing?> = emptyList(),
  val outcomes: List<AdjudicationsOutcomeHistory> = emptyList(),
  val punishments: List<AdjudicationsPunishment> = emptyList(),
  val punishmentComments: List<AdjudicationsPunishmentComment> = emptyList(),
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
        associatedPrisonersNumber = this.incidentRole?.associatedPrisonersNumber,
        associatedPrisonersName = this.incidentRole?.associatedPrisonersName,
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
        victimStaffUsername = this.offenceDetails?.victimStaffUsername,
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
      outcomes = this.outcomes.map {
        OutcomeHistoryDto(
          hearing = HearingDto(
            id = it.hearing?.id,
            locationId = it.hearing?.locationId,
            dateTimeOfHearing = it.hearing?.dateTimeOfHearing,
            oicHearingType = it.hearing?.oicHearingType,
            outcome = HearingOutcomeDto(
              id = it.hearing?.outcome?.id,
              adjudicator = it.hearing?.outcome?.adjudicator,
              code = it.hearing?.outcome?.code,
              reason = it.hearing?.outcome?.reason,
              details = it.hearing?.outcome?.details,
              plea = it.hearing?.outcome?.plea,
            ),
            agencyId = it.hearing?.agencyId,
          ),
          outcome = CombinedOutcomeDto(
            outcome = OutcomeDto(
              id = it.outcome?.outcome?.id,
              code = it.outcome?.outcome?.code,
              details = it.outcome?.outcome?.details,
              reason = it.outcome?.outcome?.reason,
              quashedReason = it.outcome?.outcome?.quashedReason,
              canRemove = it.outcome?.outcome?.canRemove,
            ),
            referralOutcome = OutcomeDto(
              id = it.outcome?.referralOutcome?.id,
              code = it.outcome?.referralOutcome?.code,
              details = it.outcome?.referralOutcome?.details,
              reason = it.outcome?.referralOutcome?.reason,
              quashedReason = it.outcome?.referralOutcome?.quashedReason,
              canRemove = it.outcome?.referralOutcome?.canRemove,
            ),
          ),
        )
      },
      punishments = this.punishments.map {
        PunishmentDto(
          id = it.id,
          type = it.type,
          privilegeType = it.privilegeType,
          otherPrivilege = it.otherPrivilege,
          stoppagePercentage = it.stoppagePercentage,
          activatedBy = it.activatedBy,
          activatedFrom = it.activatedFrom,
          schedule = PunishmentScheduleDto(
            days = it.schedule?.days,
            startDate = it.schedule?.startDate,
            endDate = it.schedule?.endDate,
            suspendedUntil = it.schedule?.suspendedUntil,
          ),
          consecutiveChargeNumber = it.consecutiveChargeNumber,
          consecutiveReportAvailable = it.consecutiveReportAvailable,
          damagesOwedAmount = it.damagesOwedAmount,
          canRemove = it.canRemove,
        )
      },
      punishmentComments = this.punishmentComments.map {
        PunishmentCommentDto(
          id = it.id,
          comment = it.comment,
          reasonForChange = it.reasonForChange,
          createdByUserId = it.createdByUserId,
          dateTime = it.dateTime,
        )
      },
    )
}
