package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import com.fasterxml.jackson.annotation.JsonValue

enum class HmppsMessageEventType(
  val type: String,
  @JsonValue val eventTypeCode: String,
  val description: String,
) {
  EXPRESSION_OF_INTEREST_CREATED(
    type = "mjma-jobs-board.job.expression-of-interest.created",
    eventTypeCode = "ExpressionOfInterestCreated",
    description = "An expression of interest has been created",
  ),
  VISIT_CREATED(
    type = "visit-someone-in-prison.visit.created",
    eventTypeCode = "VisitCreated",
    description = "An visit has been created",
  ),
  VISIT_UPDATED(
    type = "visit-someone-in-prison.visit.updated",
    eventTypeCode = "VisitUpdated",
    description = "An visit has been updated",
  ),
  VISIT_CANCELLED(
    type = "visit-someone-in-prison.visit.cancelled",
    eventTypeCode = "VisitCancelled",
    description = "An visit has been cancelled",
  ),
}
