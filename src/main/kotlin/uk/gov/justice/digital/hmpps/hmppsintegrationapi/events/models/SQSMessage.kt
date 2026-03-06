package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Message received from SQS Listener
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class SQSMessage(
  @JsonProperty("Type") val type: String,
  @JsonProperty("Message") val message: String,
  @JsonProperty("MessageId") val messageId: String,
  @JsonProperty("MessageAttributes") val messageAttributes: SQSMessageAttributes,
)
