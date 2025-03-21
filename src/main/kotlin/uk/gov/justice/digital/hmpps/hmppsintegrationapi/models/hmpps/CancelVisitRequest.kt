package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "Private prison visit cancellation request")
data class CancelVisitRequest(
  @Schema(description = "Visit Reference", example = "Reference ID in the client system", required = true)
  @field:NotBlank
  val visitReference: String,
  @Schema(description = "Outcome status and description", required = true)
  val cancelOutcome: CancelOutcome,
  @Schema(description = "Username for user who actioned this request", required = false)
  val actionedBy: String?,
) {
  fun toHmppsMessage(who: String): HmppsMessage =
    HmppsMessage(
      eventType = HmppsMessageEventType.VISIT_CANCELLED,
      messageAttributes = modelToMap(),
      who = who,
    )

  private fun modelToMap(): Map<String, Any?> =
    mapOf(
      "visitReference" to this.visitReference,
      "cancelOutcome" to this.cancelOutcome,
      "actionedBy" to this.actionedBy,
    )
}

data class CancelOutcome(
  val outcomeStatus: OutcomeStatus,
  val text: String,
)

enum class OutcomeStatus {
  ADMINISTRATIVE_CANCELLATION,
  ADMINISTRATIVE_ERROR,
  BATCH_CANCELLATION,
  CANCELLATION,
  COMPLETED_NORMALLY,
  ESTABLISHMENT_CANCELLED,
  NOT_RECORDED,
  NO_VISITING_ORDER,
  PRISONER_CANCELLED,
  PRISONER_COMPLETED_EARLY,
  PRISONER_REFUSED_TO_ATTEND,
  TERMINATED_BY_STAFF,
  VISITOR_CANCELLED,
  VISITOR_COMPLETED_EARLY,
  VISITOR_DECLINED_ENTRY,
  VISITOR_DID_NOT_ARRIVE,
  VISITOR_FAILED_SECURITY_CHECKS,
  VISIT_ORDER_CANCELLED,
  SUPERSEDED_CANCELLATION,
  DETAILS_CHANGED_AFTER_BOOKING,
  BOOKER_CANCELLED,
}
