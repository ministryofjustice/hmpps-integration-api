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
  val status: String? = null,
  val statusReason: String? = null,
  val statusDetails: String? = null,
  val hearings: List<AdjudicationsHearing?> = emptyList(),
  val outcomes: List<AdjudicationsOutcomeHistory> = emptyList(),
  val punishments: List<AdjudicationsPunishment> = emptyList(),
  val punishmentComments: List<AdjudicationsPunishmentComment> = emptyList(),
) {
  fun toAdjudication(): Adjudication =
    Adjudication(
      incidentDetails = IncidentDetailsDto(
        dateTimeOfIncident = this.incidentDetails.dateTimeOfIncident,
      ),
      isYouthOffender = this.isYouthOffender,
      incidentRole = IncidentRoleDto(
        roleCode = this.incidentRole?.roleCode,
        offenceRule = OffenceRuleDetailsDto(
          paragraphNumber = this.incidentRole?.offenceRule?.paragraphNumber,
          paragraphDescription = this.incidentRole?.offenceRule?.paragraphDescription,
        ),
      ),
      offenceDetails = OffenceDto(
        offenceCode = this.offenceDetails?.offenceCode,
        offenceRule = OffenceRuleDto(
          paragraphNumber = this.offenceDetails?.offenceRule?.paragraphNumber,
          paragraphDescription = this.offenceDetails?.offenceRule?.paragraphDescription,
        ),
      ),
      status = this.status,
      statusReason = this.statusReason,
      statusDetails = this.statusDetails,
      hearings = this.hearings.map {
        HearingDto(
          dateTimeOfHearing = it?.dateTimeOfHearing,
          oicHearingType = it?.oicHearingType,
          outcome = HearingOutcomeDto(
            code = it?.outcome?.code,
            reason = it?.outcome?.reason,
            details = it?.outcome?.details,
            plea = it?.outcome?.plea,
          ),
        )
      },
      outcomes = this.outcomes.map {
        OutcomeHistoryDto(
          hearing = HearingDto(
            dateTimeOfHearing = it.hearing?.dateTimeOfHearing,
            oicHearingType = it.hearing?.oicHearingType,
            outcome = HearingOutcomeDto(
              code = it.hearing?.outcome?.code,
              reason = it.hearing?.outcome?.reason,
              details = it.hearing?.outcome?.details,
              plea = it.hearing?.outcome?.plea,
            ),
          ),
          outcome = CombinedOutcomeDto(
            outcome = OutcomeDto(
              code = it.outcome?.outcome?.code,
              details = it.outcome?.outcome?.details,
              reason = it.outcome?.outcome?.reason,
              quashedReason = it.outcome?.outcome?.quashedReason,
              canRemove = it.outcome?.outcome?.canRemove,
            ),
            referralOutcome = OutcomeDto(
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
          type = it.type,
          privilegeType = it.privilegeType,
          otherPrivilege = it.otherPrivilege,
          schedule = PunishmentScheduleDto(
            days = it.schedule?.days,
            startDate = it.schedule?.startDate,
            endDate = it.schedule?.endDate,
            suspendedUntil = it.schedule?.suspendedUntil,
          ),
        )
      },
      punishmentComments = this.punishmentComments.map {
        PunishmentCommentDto(
          comment = it.comment,
          reasonForChange = it.reasonForChange,
          dateTime = it.dateTime,
        )
      },
    )
}
