package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class ExpressionOfInterestMessage(
  val messageId: String,
  val jobId: String,
  val prisonNumber: String,
  val eventType: EventType = EventType.EXPRESSION_OF_INTEREST_MESSAGE_CREATED,
) {
  enum class EventType {
    EXPRESSION_OF_INTEREST_MESSAGE_CREATED,
  }
}
