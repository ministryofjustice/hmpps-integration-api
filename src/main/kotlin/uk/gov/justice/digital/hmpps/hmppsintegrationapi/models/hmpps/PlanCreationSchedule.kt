package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

data class PlanCreationSchedules(
  @Schema(
    description = """
       A list of plan creation schedule versions for this prisoner.
       The prisoner will only have one plan creation schedule but for various reasons the schedule can be updated.
       This list will show each change the schedule has been through. This allows for reports to be generated.
    """,
  )
  val planCreationSchedules: List<PlanCreationSchedule>,
)

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.USE_DEFAULTS)
@JsonDeserialize(using = PlanCreationScheduleDeserializer::class)
data class PlanCreationSchedule(
  @Schema(description = "The unique reference of this plan creation schedule", example = "c88a6c48-97e2-4c04-93b5-98619966447b", required = true)
  val reference: UUID,
  @Schema(description = "Status of the schedule", example = "SCHEDULED", required = true)
  val status: PlanCreationStatus,
  @Schema(description = "The DPS username of the person who created this resource.", example = "asmith_gen", required = true)
  val createdBy: String,
  @Schema(description = "The display name of the person who created this resource.", example = "Alex Smith", required = true)
  val createdByDisplayName: String,
  @Schema(description = "When this resource was created", example = "2023-06-19T09:39:44Z", required = true)
  val createdAt: OffsetDateTime,
  @Schema(description = "Prison at creation time", example = "BXI", required = true)
  val createdAtPrison: String,
  @Schema(description = "The DPS username of the person who last updated this resource.", example = "asmith_gen", required = true)
  val updatedBy: String,
  @Schema(description = "The display name of the person who last updated this resource.", example = "Alex Smith", required = true)
  val updatedByDisplayName: String,
  @Schema(description = "When this resource was last updated", example = "2023-06-19T09:39:44Z", required = true)
  val updatedAt: OffsetDateTime,
  @Schema(description = "Prison at last update", example = "BXI", required = true)
  val updatedAtPrison: String,
  @Schema(description = "Date the plan creation is due", example = "2023-11-19")
  val deadlineDate: LocalDate? = null,
  @Schema(example = "2023-11-19", description = "If the status of this Plan Creation Schedule is COMPLETED, this field is an ISO-8601 date representing  date that the Education Support Plan was created. This field will only have a value when the status of the  Plan Creation Schedule is COMPLETED, and reflects the date the Education Support Plan was created  (rather than the Plan Creation Schedule) ")
  val planCompletedDate: LocalDate? = null,
  @Schema(example = "null", description = "If the status of this Plan Creation Schedule is COMPLETED, and the person who met with the  prisoner to create their Education Support Plan was not the same person who keyed it into  the SAN service, this field will be that person's name. This field will only have a value  when the status of the Plan Creation Schedule is COMPLETED, and the person who met with  the prisoner to create their Education Support Plan was not the same person who keyed it into the SAN service. If the Plan Creation Schedule is COMPLETED and this field is null,  consumers of this API can assume the person who created the Education Support Plan is  the person who keyed it in. See field planKeyedInBy ")
  val planCompletedBy: String? = null,
  @Schema(example = "null", description = "If the status of this Plan Creation Schedule is COMPLETED, this field is the DPS  username of the user that keyed the Education Support Plan into the system.This  field will only have a value when the status of the Plan Creation Schedule is COMPLETED,  and reflects the logged in user who interacted with the SAN service. ")
  val planKeyedInBy: String? = null,
  @Schema(example = "Education coordinator", description = "The job role of the person who completed the plan.")
  val planCompletedByJobRole: String? = null,
  @Schema(description = "Reason for exemption", example = "EXEMPT_NOT_REQUIRED")
  val exemptionReason: String? = null,
  @Schema(description = "Details about the exemption")
  val exemptionDetail: String? = null,
  @Schema(description = "The source(s) that triggered the need for a plan")
  val needSources: List<NeedSource>? = null,
  @Schema(description = "Version number of this schedule")
  val version: Int? = null,
)

class PlanCreationScheduleDeserializer : JsonDeserializer<PlanCreationSchedule>() {
  override fun deserialize(
    parser: JsonParser,
    ctxt: DeserializationContext,
  ): PlanCreationSchedule {
    val node = parser.codec.readTree<JsonNode>(parser)

    return PlanCreationSchedule(
      reference = UUID.fromString(node["reference"].asText()),
      status = PlanCreationStatus.valueOf(node["status"].asText()),
      createdBy = node["createdBy"].asText(),
      createdByDisplayName = node["createdByDisplayName"].asText(),
      createdAt = OffsetDateTime.parse(node["createdAt"].asText()),
      createdAtPrison = node["createdAtPrison"].asText(),
      updatedBy = node["updatedBy"].asText(),
      updatedByDisplayName = node["updatedByDisplayName"].asText(),
      updatedAt = OffsetDateTime.parse(node["updatedAt"].asText()),
      updatedAtPrison = node["updatedAtPrison"].asText(),
      deadlineDate = node["deadlineDate"]?.takeUnless { it.isNull }?.asText()?.let { LocalDate.parse(it) },
      exemptionReason = node["exemptionReason"]?.takeUnless { it.isNull }?.asText(),
      exemptionDetail = node["exemptionDetail"]?.takeUnless { it.isNull }?.asText(),
      needSources =
        node["needSources"]?.takeUnless { it.isNull }?.mapNotNull { ns ->
          ns?.asText()?.let { NeedSource.valueOf(it) }
        },
      planKeyedInBy = node["planKeyedInBy"]?.takeUnless { it.isNull }?.asText(),
      planCompletedDate = node["planCompletedDate"]?.takeUnless { it.isNull }?.asText()?.let { LocalDate.parse(it) },
      planCompletedBy = node["planCompletedBy"]?.takeUnless { it.isNull }?.asText(),
      planCompletedByJobRole = node["planCompletedByJobRole"]?.takeUnless { it.isNull }?.asText(),
      version = node["version"]?.takeUnless { it.isNull }?.asInt(),
    )
  }
}

enum class NeedSource(
  val value: String,
) {
  LDD_SCREENER("LDD_SCREENER"),
  ALN_SCREENER("ALN_SCREENER"),
  CONDITION_SELF_DECLARED("CONDITION_SELF_DECLARED"),
  CONDITION_CONFIRMED_DIAGNOSIS("CONDITION_CONFIRMED_DIAGNOSIS"),
  CHALLENGE_NOT_ALN_SCREENER("CHALLENGE_NOT_ALN_SCREENER"),
}

enum class PlanCreationStatus(
  val value: String,
) {
  SCHEDULED("SCHEDULED"),
  EXEMPT_SYSTEM_TECHNICAL_ISSUE("EXEMPT_SYSTEM_TECHNICAL_ISSUE"),
  EXEMPT_PRISONER_TRANSFER("EXEMPT_PRISONER_TRANSFER"),
  EXEMPT_PRISONER_RELEASE("EXEMPT_PRISONER_RELEASE"),
  EXEMPT_PRISONER_DEATH("EXEMPT_PRISONER_DEATH"),
  EXEMPT_PRISONER_MERGE("EXEMPT_PRISONER_MERGE"),
  EXEMPT_PRISONER_NOT_COMPLY("EXEMPT_PRISONER_NOT_COMPLY"),
  EXEMPT_NOT_IN_EDUCATION("EXEMPT_NOT_IN_EDUCATION"),
  EXEMPT_NO_NEED("EXEMPT_NO_NEED"),
  EXEMPT_UNKNOWN("EXEMPT_UNKNOWN"),
  COMPLETED("COMPLETED"),
}
