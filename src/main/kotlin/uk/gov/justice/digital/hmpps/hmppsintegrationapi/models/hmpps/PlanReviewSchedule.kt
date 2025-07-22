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

data class PlanReviewSchedules(
  @Schema(
    description = """
       A list of plan review schedule versions for this prisoner.
       The prisoner will many review schedules for each review, and the schedule can changed depending on
       the various phases the review has been through. eg SCHEDULED, EXEMPT, COMPLETED, the reference can be used to group the reviews.
       This list will show each change the schedule has been through. This allows for reports to be generated.
    """,
  )
  @JsonProperty("reviewSchedules")
  val planReviewSchedules: List<PlanReviewSchedule>,
)

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.USE_DEFAULTS)
@JsonDeserialize(using = ReviewScheduleDeserializer::class)
/**
 * A Scheduled Review of a Prisoner's support additional needs plan.  A DPS user will create a new Review Schedule (and therefore they will be recorded in the `createdBy...` fields) when they complete a prisoner's additional needs plan or their previous Review. IE. completing the plan will create the prisoner's first Review Schedule. Completing their Review will create their next Review Schedule.
 * @param reference The unique reference of this Review
 * @param deadlineDate An ISO-8601 date representing date that the Review is due.
 * @param status
 * @param createdBy The DPS username of the person who created this resource.
 * @param createdByDisplayName The display name of the person who created this resource.
 * @param createdAt An ISO-8601 timestamp representing when this resource was created.
 * @param createdAtPrison The identifier of the prison that the prisoner was resident at when this resource was created.
 * @param updatedBy The DPS username of the person who last updated this resource.
 * @param updatedByDisplayName The display name of the person who last updated this resource.
 * @param updatedAt An ISO-8601 timestamp representing when this resource was last updated. This will be the same as the created date if it has not yet been updated.
 * @param updatedAtPrison The identifier of the prison that the prisoner was resident at when this resource was updated.
 * @param reviewCompletedDate If the status of this Review Schedule is COMPLETED, this field is an ISO-8601 date representing  date that the Review was created. This field will only have a value when the status of the  Review Schedule is COMPLETED, and reflects the date the Review was completed.
 * @param reviewKeyedInBy If the status of this Review Schedule is COMPLETED, this field is the DPS  username of the user that keyed the Review into the system.This  field will only have a value when the status of the Review Schedule is COMPLETED,  and reflects the logged in user who interacted with the SAN service.
 * @param reviewCompletedBy If the status of this Review Schedule is COMPLETED, and the person who met with the  prisoner to create their Review was not the same person who keyed it into  the SAN service, this field will be that person's name. This field will only have a value  when the status of the Review Schedule is COMPLETED, and the person who met with  the prisoner to create their Education Support Plan was not the same person who keyed it into the SAN service. If the Review Schedule is COMPLETED and this field is null,  consumers of this API can assume the person who created the Review is  the person who keyed it in. See field reviewKeyedInBy
 * @param reviewCompletedByJobRole The job role of the person who completed the review.
 * @param exemptionReason An optional reason as to why the Review Schedule is exempted.  Only present when the `status` field is one of the `EXEMPTION_` statuses and the user entered an exemption  reason when marking the Review as Exempted.
 * @param version the version number of this schedule (the highest number is the most recent version of this review schedule)
 */
data class PlanReviewSchedule(
  @Schema(example = "c88a6c48-97e2-4c04-93b5-98619966447b", required = true, description = "The unique reference of this Review")
  @get:JsonProperty("reference", required = true) val reference: UUID,
  @field:Valid
  @Schema(example = "2023-11-19", required = true, description = "An ISO-8601 date representing date that the Review is due. ")
  @get:JsonProperty("deadlineDate", required = true) val deadlineDate: LocalDate,
  @field:Valid
  @Schema(example = "null", required = true, description = "")
  @get:JsonProperty("status", required = true) val status: PlanReviewScheduleStatus,
  @Schema(example = "asmith_gen", required = true, description = "The DPS username of the person who created this resource.")
  @get:JsonProperty("createdBy", required = true) val createdBy: String,
  @Schema(example = "Alex Smith", required = true, description = "The display name of the person who created this resource.")
  @get:JsonProperty("createdByDisplayName", required = true) val createdByDisplayName: String,
  @Schema(example = "2023-06-19T09:39:44Z", required = true, description = "An ISO-8601 timestamp representing when this resource was created.")
  @get:JsonProperty("createdAt", required = true) val createdAt: java.time.OffsetDateTime,
  @Schema(example = "BXI", required = true, description = "The identifier of the prison that the prisoner was resident at when this resource was created.")
  @get:JsonProperty("createdAtPrison", required = true) val createdAtPrison: String,
  @Schema(example = "asmith_gen", required = true, description = "The DPS username of the person who last updated this resource.")
  @get:JsonProperty("updatedBy", required = true) val updatedBy: String,
  @Schema(example = "Alex Smith", required = true, description = "The display name of the person who last updated this resource.")
  @get:JsonProperty("updatedByDisplayName", required = true) val updatedByDisplayName: String,
  @Schema(example = "2023-06-19T09:39:44Z", required = true, description = "An ISO-8601 timestamp representing when this resource was last updated. This will be the same as the created date if it has not yet been updated.")
  @get:JsonProperty("updatedAt", required = true) val updatedAt: OffsetDateTime,
  @Schema(example = "BXI", required = true, description = "The identifier of the prison that the prisoner was resident at when this resource was updated.")
  @get:JsonProperty("updatedAtPrison", required = true) val updatedAtPrison: String,
  @field:Valid
  @Schema(example = "2023-11-19", description = "If the status of this Review Schedule is COMPLETED, this field is an ISO-8601 date representing  date that the Review was created. This field will only have a value when the status of the  Review Schedule is COMPLETED, and reflects the date the Review was completed. ")
  @get:JsonProperty("reviewCompletedDate") val reviewCompletedDate: LocalDate? = null,
  @Schema(example = "null", description = "If the status of this Review Schedule is COMPLETED, this field is the DPS  username of the user that keyed the Review into the system.This  field will only have a value when the status of the Review Schedule is COMPLETED,  and reflects the logged in user who interacted with the SAN service. ")
  @get:JsonProperty("reviewKeyedInBy") val reviewKeyedInBy: String? = null,
  @Schema(example = "null", description = "If the status of this Review Schedule is COMPLETED, and the person who met with the  prisoner to create their Review was not the same person who keyed it into  the SAN service, this field will be that person's name. This field will only have a value  when the status of the Review Schedule is COMPLETED, and the person who met with  the prisoner to create their Education Support Plan was not the same person who keyed it into the SAN service. If the Review Schedule is COMPLETED and this field is null,  consumers of this API can assume the person who created the Review is  the person who keyed it in. See field reviewKeyedInBy ")
  @get:JsonProperty("reviewCompletedBy") val reviewCompletedBy: String? = null,
  @Schema(example = "Education coordinator", description = "The job role of the person who completed the review.")
  @get:JsonProperty("reviewCompletedByJobRole") val reviewCompletedByJobRole: String? = null,
  @Schema(example = "null", description = "An optional reason as to why the Review Schedule is exempted.  Only present when the `status` field is one of the `EXEMPTION_` statuses and the user entered an exemption  reason when marking the Review as Exempted. ")
  @get:JsonProperty("exemptionReason") val exemptionReason: String? = null,
  @Schema(example = "null", description = "the version number of this schedule (the highest number is the most recent version of this review schedule)")
  @get:JsonProperty("version") val version: Int? = null,
)

class ReviewScheduleDeserializer : JsonDeserializer<PlanReviewSchedule>() {
  override fun deserialize(
    parser: JsonParser,
    ctxt: DeserializationContext,
  ): PlanReviewSchedule {
    val node = parser.codec.readTree<JsonNode>(parser)

    return PlanReviewSchedule(
      reference = UUID.fromString(node["reference"].asText()),
      status = PlanReviewScheduleStatus.valueOf(node["status"].asText()),
      createdBy = node["createdBy"].asText(),
      createdByDisplayName = node["createdByDisplayName"].asText(),
      createdAt = OffsetDateTime.parse(node["createdAt"].asText()),
      createdAtPrison = node["createdAtPrison"].asText(),
      updatedBy = node["updatedBy"].asText(),
      updatedByDisplayName = node["updatedByDisplayName"].asText(),
      updatedAt = OffsetDateTime.parse(node["updatedAt"].asText()),
      updatedAtPrison = node["updatedAtPrison"].asText(),
      deadlineDate = LocalDate.parse(node["deadlineDate"].asText()),
      exemptionReason = node["exemptionReason"]?.takeUnless { it.isNull }?.asText(),
      reviewKeyedInBy = node["reviewKeyedInBy"]?.takeUnless { it.isNull }?.asText(),
      reviewCompletedDate = node["reviewCompletedDate"]?.takeUnless { it.isNull }?.asText()?.let { LocalDate.parse(it) },
      reviewCompletedBy = node["reviewCompletedBy"]?.takeUnless { it.isNull }?.asText(),
      reviewCompletedByJobRole = node["reviewCompletedByJobRole"]?.takeUnless { it.isNull }?.asText(),
      version = node["version"]?.takeUnless { it.isNull }?.asInt(),
    )
  }
}

enum class PlanReviewScheduleStatus(
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
