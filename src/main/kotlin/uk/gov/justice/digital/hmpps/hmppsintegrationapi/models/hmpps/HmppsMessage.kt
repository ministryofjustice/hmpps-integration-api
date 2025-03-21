package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import java.util.UUID

data class HmppsMessage(
  val messageId: String = UUID.randomUUID().toString(),
  val eventType: HmppsMessageEventType,
  val description: String? = null,
  val messageAttributes: Map<String, Any?> = emptyMap(),
  val who: String? = null,
)
