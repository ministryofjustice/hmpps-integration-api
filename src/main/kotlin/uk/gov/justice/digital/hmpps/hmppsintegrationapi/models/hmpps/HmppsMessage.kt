package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class HmppsMessage(
  val messageId: String,
  val eventType: HmppsMessageEventType,
  val description: String? = null,
  val messageAttributes: Map<String, String> = emptyMap(),
)
