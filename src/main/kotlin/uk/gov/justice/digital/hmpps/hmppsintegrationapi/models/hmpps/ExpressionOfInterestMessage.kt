package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import java.util.UUID

data class ExpressionOfInterestMessage(
  val messageId: String = UUID.randomUUID().toString(),
  val jobId: String,
  val prisonNumber: String,
  val eventType: MessageType = MessageType.EXPRESSION_OF_INTEREST_CREATED,
)
