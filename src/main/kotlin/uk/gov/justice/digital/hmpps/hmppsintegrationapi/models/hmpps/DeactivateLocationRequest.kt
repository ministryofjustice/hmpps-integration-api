package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Size
import java.time.LocalDate

@Schema(description = "Deactivate location request")
data class DeactivateLocationRequest(
  @Schema(description = "Reason for temporary deactivation", example = "MOTHBALLED", required = true)
  val deactivationReason: DeactivationReason,
  @Schema(
    description = "Additional information on deactivation, for OTHER DeactivatedReason must be provided",
    example = "Window broken",
    required = false,
  )
  @field:Size(max = 255, message = "Other deactivation reason cannot be more than 255 characters")
  val deactivationReasonDescription: String? = null,
  @Schema(description = "Estimated reactivation date", example = "2025-01-05", required = false)
  val proposedReactivationDate: LocalDate? = null,
  @Schema(description = "External reference", required = false)
  val externalReference: String? = null,
) {
  fun toHmppsMessage(
    locationId: String,
    actionedBy: String?,
  ): HmppsMessage =
    HmppsMessage(
      eventType = HmppsMessageEventType.LOCATION_DEACTIVATE,
      messageAttributes = modelToMap(locationId),
      who = actionedBy,
    )

  private fun modelToMap(locationId: String): Map<String, Any?> =
    mapOf(
      "id" to locationId,
      "deactivationReason" to this.deactivationReason,
      "deactivationReasonDescription" to this.deactivationReasonDescription,
      "proposedReactivationDate" to this.proposedReactivationDate.toString(),
      "planetFmReference" to null,
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
