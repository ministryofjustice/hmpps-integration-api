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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.models.EventType
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.models.SQSMessageAttributes
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.telemetry.TelemetryService
import uk.gov.justice.hmpps.sqs.HmppsQueueService

@ConditionalOnProperty("feature-flag.${FeatureFlagConfig.ENABLE_PUBLISH_PENDING_EVENTS}", havingValue = "true")
@Service
class EventNotificationService(
  private val hmppsQueueService: HmppsQueueService,
  private val objectMapper: ObjectMapper,
  private val authorisationConfig: AuthorisationConfig,
  private val featureFlagConfig: FeatureFlagConfig,
  private val telemetryService: TelemetryService,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun sendEvent(event: EventNotification) {
    if (featureFlagConfig.isEnabled(FeatureFlagConfig.DIRECT_SQS_NOTIFICATIONS)) {
      return sendEventToQueue(event)
    }

    return sendEventToTopic(event)
  }

  fun sendEventToTopic(payload: EventNotification) {
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
    var messagesSent = 0
    authorisationConfig.consumersWithQueue().forEach { consumer ->
      if (isEventApplicable(consumer, event)) {
        val queueName = authorisationConfig.consumers[consumer]?.queueName!!
        try {
          val message =
            DirectSQSMessage(
              message = objectMapper.writeValueAsString(event),
              messageAttributes = SQSMessageAttributes(EventType(event.eventType)),
            )
          val queue = hmppsQueueService.findByQueueId(queueName)
          val sqsClient = queue?.sqsClient!!
          val sendMessageRequest =
            SendMessageRequest
              .builder()
              .queueUrl(queue.queueUrl)
              .messageBody(objectMapper.writeValueAsString(message))
              .build()
          sqsClient.sendMessage(sendMessageRequest)
          messagesSent++
          log.debug("Successfully published event ${event.eventType} to $queueName")
        } catch (ex: Exception) {
          log.error("Error publishing event ${event.eventType} to $queueName. ${ex.message}")
          telemetryService.captureException(ex)
        }
      }
    }
    log.info("Event ${event.eventType} successfully sent to $messagesSent queues")
  }

  /**
   * Checks if the event is applicable to the consumer
   */
  fun isEventApplicable(
    consumer: String,
    event: EventNotification,
  ): Boolean {
    val consumerEvents = authorisationConfig.events(consumer)
    val prisonIds = authorisationConfig.allFilters(consumer)?.prisons
    val prisonCheck = prisonIds == null || (event.prisonId != null && prisonIds.contains(event.prisonId))
    return consumerEvents.contains(event.eventType) && prisonCheck
  }
}
