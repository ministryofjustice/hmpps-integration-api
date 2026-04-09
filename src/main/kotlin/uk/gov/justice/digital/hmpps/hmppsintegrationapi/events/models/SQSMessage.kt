package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant
import java.util.UUID
import kotlin.String

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

/**
 * Message sent to consumer queues
 * Note. This is in the shape of a message that has resulted from an SNS notification to keep inline with consumers
 */
data class DirectSQSMessage(
  @JsonProperty("Type") val type: String = "Notification",
  @JsonProperty("MessageId") val messageId: String = UUID.randomUUID().toString(),
  @JsonProperty("TopicArn") val topicArn: String = "",
  @JsonProperty("Message") val message: String,
  @JsonProperty("Timestamp") val timestamp: String = "${Instant.now()}",
  @JsonProperty("SignatureVersion") val signatureVersion: String = "",
  @JsonProperty("Signature") val signature: String = "",
  @JsonProperty("SigningCertURL") val signingCertURL: String = "",
  @JsonProperty("UnsubscribeURL") val unsubscribeURL: String = "",
  @JsonProperty("MessageAttributes") val messageAttributes: SQSMessageAttributes,
)
