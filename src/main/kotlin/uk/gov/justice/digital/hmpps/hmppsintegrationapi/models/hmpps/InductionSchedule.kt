package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import tools.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.JsonNode
import tools.jackson.databind.ValueDeserializer
import tools.jackson.databind.annotation.JsonDeserialize
import java.time.Instant
import java.time.LocalDate

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.USE_DEFAULTS)
data class InductionSchedules(
  @Schema(
    description = """
       A list of induction schedule versions for this prisoner.
       The prisoner will only have one induction schedule but for various reasons the schedule can be updated.
       This list will show each change the schedule has been through. This allows for reports to be generated.
    """,
  )
  val inductionSchedules: List<InductionSchedule>,
)

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.USE_DEFAULTS)
@JsonDeserialize(using = InductionScheduleDeserializer::class)
data class InductionSchedule(
  @Schema(
    description = "An ISO-8601 date representing when the Induction should be completed by.",
    example = "2023-09-01",
  )
  val deadlineDate: LocalDate? = null,
  @Schema(
    description = """
      The current status of the Induction Schedule.
      list of values:
          SCHEDULED
          COMPLETED
          EXEMPT_PRISONER_DRUG_OR_ALCOHOL_DEPENDENCY
          EXEMPT_PRISONER_OTHER_HEALTH_ISSUES
          EXEMPT_PRISONER_FAILED_TO_ENGAGE
          EXEMPT_PRISONER_ESCAPED_OR_ABSCONDED
          EXEMPT_PRISONER_SAFETY_ISSUES
          EXEMPT_PRISON_REGIME_CIRCUMSTANCES
          EXEMPT_PRISON_STAFF_REDEPLOYMENT
          EXEMPT_PRISON_OPERATION_OR_SECURITY_ISSUE
          EXEMPT_SECURITY_ISSUE_RISK_TO_STAFF
          EXEMPT_SYSTEM_TECHNICAL_ISSUE
          EXEMPT_PRISONER_TRANSFER
          EXEMPT_PRISONER_RELEASE
          EXEMPT_PRISONER_DEATH
          EXEMPT_SCREENING_AND_ASSESSMENT_IN_PROGRESS
          EXEMPT_SCREENING_AND_ASSESSMENT_INCOMPLETE
          EXEMPT_PRISONER_MERGE
    """,
    example = "SCHEDULED",
  )
  val status: String? = null,
  @Schema(
    description = """
        The Induction Schedule rule used to determine deadline date.
        list of values are:
          NEW_PRISON_ADMISSION
          EXISTING_PRISONER_LESS_THAN_6_MONTHS_TO_SERVE
          EXISTING_PRISONER_BETWEEN_6_AND_12_MONTHS_TO_SERVE
          EXISTING_PRISONER_BETWEEN_12_AND_60_MONTHS_TO_SERVE
          EXISTING_PRISONER_INDETERMINATE_SENTENCE
          EXISTING_PRISONER_ON_REMAND
          EXISTING_PRISONER_UN_SENTENCED
    """,
    example = "NEW_PRISON_ADMISSION",
  )
  val calculationRule: String? = null,
  @Schema(
    description = "The Nomis number of the person.",
    example = "A1234BC",
  )
  val nomisNumber: String? = null,
  @Schema(
    description =
      "The name of the person who used the PLP system to create the Induction Schedule, " +
        "or 'system' for system generated updates.",
    example = "John Smith",
  )
  val systemCreatedBy: String? = null,
  @Schema(
    description = "An ISO-8601 timestamp representing the time the PLP system was used to create the Induction Schedule.",
    example = "2023-06-19T09:39:44Z",
  )
  val systemCreatedAt: Instant? = null,
  @Schema(
    description =
      "The code of the prison where the induction was Created",
    example = "BXI",
  )
  val systemCreatedAtPrison: String? = null,
  @Schema(
    description = """
      The name of the person who performed the Induction with the prisoner.
      In the case of system generated updates or setting an exemption this field will not be present.
    """,
    example = "Fred Jones",
  )
  val systemUpdatedBy: String? = null,
  @Schema(
    description = "An ISO-8601 timestamp representing the time the PLP system was used to update the Induction Schedule.",
    example = "2023-06-19T09:39:44Z",
  )
  val systemUpdatedAt: Instant? = null,
  @Schema(
    description =
      "The code of the prison where the induction was updated",
    example = "BXI",
  )
  val systemUpdatedAtPrison: String? = null,
  @Schema(
    description = """
      The name of the person who performed the Induction with the prisoner.
      In the case of system generated updates or setting an exemption this field will not be present.
    """,
    example = "Fred Jones",
  )
  val inductionPerformedBy: String? = null,
  @Schema(
    description = """
      When the induction schedule has a completed induction this will be populated with the date that the CIAG entered into the system when completing the Induction. It is the date that the user did the Induction in person, rather than the date that it was entered into the system.
    """,
    example = "2023-06-30",
  )
  val inductionPerformedAt: LocalDate? = null,
  @Schema(
    description = """
      The role of the person who performed the Induction with the prisoner.
      In the case of system generated updates or setting an exemption this field will not be present.
    """,
    example = "Peer Mentor",
  )
  val inductionPerformedByRole: String? = null,
  @Schema(
    description = "The prison code that the induction was performed at.",
    example = "BXI",
  )
  val inductionPerformedAtPrison: String? = null,
  @Schema(
    description = "If reason the induction schedule was exempted.",
    example = "EXEMPT_SYSTEM_TECHNICAL_ISSUE",
  )
  val exemptionReason: String? = null,
  @Schema(
    description = """
      The induction schedule can change status numerous times.
      When looking at the plp-induction-schedule/history of the inductions.
      The version number indicates which version of the induction schedule this one is,
      the higher the number the newer the update.
    """,
  )
  val version: Int? = null,
)

class InductionScheduleDeserializer : ValueDeserializer<InductionSchedule>() {
  override fun deserialize(
    parser: JsonParser,
    ctxt: DeserializationContext,
  ): InductionSchedule {
    val node = parser.objectReadContext().readTree<JsonNode>(parser)
    val nomisNumber = node["prisonNumber"]?.asString()
    val deadlineDate = node["deadlineDate"]?.asString()?.let { LocalDate.parse(it) }
    val scheduleStatus = node["scheduleStatus"]?.asString()
    val scheduleCalculationRule = node["scheduleCalculationRule"]?.asString()
    val systemUpdatedBy = node["updatedByDisplayName"]?.asString()
    val systemUpdatedAt = node["updatedAt"]?.asString()?.let { Instant.parse(it) }
    val systemUpdatedAtPrison = node["updatedAtPrison"]?.asString()
    val systemCreatedBy = node["createdByDisplayName"]?.asString()
    val systemCreatedAt = node["createdAt"]?.asString()?.let { Instant.parse(it) }
    val systemCreatedAtPrison = node["createdAtPrison"]?.asString()
    val inductionPerformedBy = node["inductionPerformedBy"]?.takeUnless { it.isNull }?.asString()
    val inductionPerformedAt = node["inductionPerformedAt"]?.takeUnless { it.isNull }?.asString()?.let { LocalDate.parse(it) }
    val inductionPerformedByRole = node["inductionPerformedByRole"]?.takeUnless { it.isNull }?.asString()
    val inductionPerformedAtPrison = node["inductionPerformedAtPrison"]?.takeUnless { it.isNull }?.asString()
    val version = node["version"]?.asInt()
    val exemptionReason = node["exemptionReason"]?.takeUnless { it.isNull }?.asText()

    return InductionSchedule(
      deadlineDate = deadlineDate,
      status = scheduleStatus,
      calculationRule = scheduleCalculationRule,
      nomisNumber = nomisNumber,
      systemCreatedBy = systemCreatedBy,
      systemCreatedAt = systemCreatedAt,
      systemCreatedAtPrison = systemCreatedAtPrison,
      systemUpdatedBy = systemUpdatedBy,
      systemUpdatedAt = systemUpdatedAt,
      systemUpdatedAtPrison = systemUpdatedAtPrison,
      inductionPerformedBy = inductionPerformedBy,
      inductionPerformedAt = inductionPerformedAt,
      inductionPerformedByRole = inductionPerformedByRole,
      inductionPerformedAtPrison = inductionPerformedAtPrison,
      exemptionReason = exemptionReason,
      version = version,
    )
  }
}
