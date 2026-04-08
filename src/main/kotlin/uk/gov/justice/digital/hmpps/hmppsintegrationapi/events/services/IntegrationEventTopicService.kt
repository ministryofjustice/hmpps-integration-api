package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.sns.model.MessageAttributeValue
import software.amazon.awssdk.services.sns.model.PublishRequest
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.AuthorisationConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.EventNotification
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.enums.INTEGRATION_EVENT_TOPIC
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.models.DirectSQSMessage
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.telemetry.TelemetryService
import uk.gov.justice.hmpps.sqs.HmppsQueueService

@ConditionalOnProperty("feature-flag.${FeatureFlagConfig.ENABLE_PUBLISH_PENDING_EVENTS}", havingValue = "true")
@Service
class IntegrationEventTopicService(
  private val hmppsQueueService: HmppsQueueService,
  private val objectMapper: ObjectMapper,
  private val authorisationConfig: AuthorisationConfig,
  private val featureFlagConfig: FeatureFlagConfig,
  private val telemetryService: TelemetryService,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun sendEvent(payload: EventNotification) {
    if (featureFlagConfig.isEnabled(FeatureFlagConfig.DIRECT_SQS_NOTIFICATIONS)) {
      return sendEventToQueue(payload)
    }

    val hmppsEventTopic = hmppsQueueService.findByTopicId(INTEGRATION_EVENT_TOPIC)
    val topicArn = hmppsEventTopic!!.arn
    val hmppsEventsTopicSnsClient = hmppsEventTopic.snsClient
    val messageAttributes =
      mutableMapOf(
        "eventType" to
          MessageAttributeValue
            .builder()
            .dataType("String")
            .stringValue(payload.eventType)
            .build(),
      )
    if (payload.prisonId != null) {
      messageAttributes["prisonId"] =
        MessageAttributeValue
          .builder()
          .dataType("String")
          .stringValue(payload.prisonId)
          .build()
    }
    hmppsEventsTopicSnsClient
      .publish(
        PublishRequest
          .builder()
          .topicArn(topicArn)
          .message(objectMapper.writeValueAsString(payload))
          .messageAttributes(messageAttributes)
          .build(),
      ).get()
    log.info("successfully published event ${payload.eventType} to integrationeventtopic. $payload")
  }

  /**
   * Determines which queues are applicable to the event and publishes directly to those queues
   *
   */
  fun sendEventToQueue(event: EventNotification) {
    authorisationConfig.consumersWithQueue().forEach { consumer ->
      if (authorisationConfig.isEventApplicable(consumer, event)) {
        val queueName = authorisationConfig.consumers[consumer]?.queueName!!
        try {
          val message = DirectSQSMessage(objectMapper.writeValueAsString(event))
          val queue = hmppsQueueService.findByQueueId(queueName)
          val sqsClient = queue?.sqsClient!!
          val sendMessageRequest =
            SendMessageRequest
              .builder()
              .queueUrl(queue.queueUrl)
              .messageBody(objectMapper.writeValueAsString(message))
              .build()
          sqsClient.sendMessage(sendMessageRequest)
        } catch (ex: Exception) {
          telemetryService.captureException(ex)
        }
      }
    }
  }
}
