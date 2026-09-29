package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.casestatus

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.OffsetDateTime

data class CaseStatusUpdate(
  @field:NotBlank(message = "status must be supplied")
  @field:ValidCaseStatus
  @JsonProperty("status")
  @Schema(
    name = "status",
    description = "New status for the case",
    example = "rejected",
    requiredMode = Schema.RequiredMode.REQUIRED,
    allowableValues = ["rejected"],
  )
  val rawStatus: String,
  @Schema(description = "Reasons associated with the status change")
  val reasons: List<@Valid CaseStatusReason>? = null,
  @field:NotNull(message = "datetimeOfStatusChange must be supplied")
  @Schema(
    description = "ISO 8601 timestamp of when the status changed",
    example = "2023-10-27T14:30:00Z",
    requiredMode = Schema.RequiredMode.REQUIRED,
  )
  val datetimeOfStatusChange: OffsetDateTime,
) {
  @get:JsonIgnore
  @get:Schema(hidden = true)
  val status: CaseStatus
    get() = CaseStatus.fromValue(rawStatus)
}

enum class CaseStatus(
  @get:JsonValue val value: String,
) {
  APPROVED("approved"),
  REJECTED("rejected"),
  PENDING("pending"),
  INSTALLED("installed"),
  SCHEDULED("scheduled"),
  UNKNOWN("unknown"),
  ;

  companion object {
    fun fromValue(value: String): CaseStatus = entries.firstOrNull { it.value == value } ?: UNKNOWN

    fun validValues(): List<String> = entries.filter { it != UNKNOWN }.map { it.value }
  }
}

data class CaseStatusReason(
  @field:NotBlank(message = "reason section must be supplied")
  @field:Size(max = 100, message = "reason section must not exceed 100 characters")
  @Schema(
    description = "Predefined reason code",
    example = "duplicate_submission",
    requiredMode = Schema.RequiredMode.REQUIRED,
  )
  val section: String,
  @field:Size(max = 1000, message = "reason details must not exceed 1000 characters")
  @Schema(
    description = "Free-text explanation",
    example = "Case was already submitted.",
  )
  val details: String? = null,
)
