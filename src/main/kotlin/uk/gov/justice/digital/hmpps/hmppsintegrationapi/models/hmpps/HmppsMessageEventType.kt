package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

enum class HmppsMessageEventType(
  val type: String,
  val eventTypeCoe: String,
  val description: String,
) {
  EXPRESSION_OF_INTEREST_CREATED(
    type = "mjma-jobs-board.job.created",
    eventTypeCoe = "ExpressionOfInterestCreated",
    description = "An expression of interest has been created",
  ),
}
