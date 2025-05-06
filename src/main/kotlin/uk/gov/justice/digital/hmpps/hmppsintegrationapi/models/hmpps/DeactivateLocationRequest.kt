package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "Deactivate location request")
data class DeactivateLocationRequest(
  @Schema(description = "Reason", example = "MAINTENANCE", required = true)
  val reason: DeactivationReason,
  @Schema(description = "Reason description", example = "Description of a reason. Required if reason is OTHER.", required = false)
  val reasonDescription: String? = null,
  @Schema(description = "Proposed reactivation date", example = "2025-01-05", required = true)
  val proposedReactivationDate: LocalDateTime,
) {
  fun toHmppsMessage(locationId: String): HmppsMessage =
    HmppsMessage(
      eventType = HmppsMessageEventType.LOCATION_DEACTIVATE,
      messageAttributes = modelToMap(locationId),
    )

  private fun modelToMap(locationId: String): Map<String, Any?> =
    mapOf(
      "reason" to this.reason,
      "reasonDescription" to this.reasonDescription,
      "proposedReactivationDate" to this.proposedReactivationDate,
      "locationId" to locationId,
    )
}

enum class DeactivationReason {
  DAMAGED,
  DAMP,
  MAINTENANCE,
  MOTHBALLED,
  PEST,
  REFURBISHMENT,
  SECURITY_SEALED,
  STAFF_SHORTAGE,
  OTHER,
}
