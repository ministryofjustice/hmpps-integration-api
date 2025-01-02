package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.time.LocalDate

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.USE_DEFAULTS)
data class InductionSchedules(
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
    description = "The current status of the Induction Schedule",
    example = "SCHEDULED",
  )
  val status: String? = null,
  @Schema(
    description = "The Induction Schedule rule used to determine deadline date.",
    example = "NEW_PRISON_ADMISSION",
  )
  val calculationRule: String? = null,
  @Schema(
    description = "The Nomis number of the person.",
    example = "A1234BC",
  )
  val nomisNumber: String? = null,
  @Schema(
    description = "The name of the person who used the PLP system to update the Induction Schedule, or 'system' for system generated updates.",
    example = "John Smith",
  )
  val systemUpdatedBy: String? = null,
  @Schema(
    description = "An ISO-8601 timestamp representing the time the PLP system was used to update the Induction Schedule.",
    example = "2023-06-19T09:39:44Z",
  )
  val systemUpdatedAt: Instant? = null,
  @Schema(
    description = "The name of the person who performed the Induction with the prisoner. In the case of system generated updates or setting an exemption this field will not be present.",
    example = "Fred Jones",
  )
  val inductionPerformedBy: String? = null,
  @Schema(
    description = "An ISO-8601 date representing when the Induction was performed with the prisoner. In the case of system generated updates this field will not be present.",
    example = "2023-06-30",
  )
  val inductionPerformedAt: LocalDate? = null,
  val exemptionReason: String? = null,
  val version: Int? = null,
)

class InductionScheduleDeserializer : JsonDeserializer<InductionSchedule>() {
  override fun deserialize(
    parser: JsonParser,
    ctxt: DeserializationContext,
  ): InductionSchedule {
    val node = parser.codec.readTree<JsonNode>(parser)
    val nomisNumber = node["prisonNumber"]?.asText()
    val deadlineDate = node["deadlineDate"]?.asText()?.let { LocalDate.parse(it) }
    val scheduleStatus = node["scheduleStatus"]?.asText()
    val scheduleCalculationRule = node["scheduleCalculationRule"]?.asText()
    val systemUpdatedBy = node["updatedByDisplayName"]?.asText()
    val systemUpdatedAt = node["updatedAt"]?.asText()?.let { Instant.parse(it) }
    val inductionPerformedBy = node["inductionPerformedBy"]?.takeUnless { it.isNull }?.asText()
    val inductionPerformedAt = node["inductionPerformedAt"]?.takeUnless { it.isNull }?.asText()?.let { LocalDate.parse(it) }
    val version = node["version"]?.asInt()
    val exemptionReason = node["exemptionReason"]?.asText()

    return InductionSchedule(
      deadlineDate = deadlineDate,
      status = scheduleStatus,
      calculationRule = scheduleCalculationRule,
      nomisNumber = nomisNumber,
      systemUpdatedBy = systemUpdatedBy,
      systemUpdatedAt = systemUpdatedAt,
      inductionPerformedBy = inductionPerformedBy,
      inductionPerformedAt = inductionPerformedAt,
      exemptionReason = exemptionReason,
      version = version,
    )
  }
}
