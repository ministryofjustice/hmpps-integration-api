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
}
