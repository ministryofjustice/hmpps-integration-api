package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.sns.model.MessageAttributeValue
import software.amazon.awssdk.services.sns.model.PublishRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.AuthorisationConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.EventNotification
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.enums.INTEGRATION_EVENT_TOPIC
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.messaging.QueueService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.models.DirectSQSMessage
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.models.EventType
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.models.SQSMessageAttributes
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.SupervisionStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.telemetry.TelemetryService
import uk.gov.justice.hmpps.sqs.HmppsQueueService

@ConditionalOnProperty("feature-flag.${FeatureFlagConfig.ENABLE_PUBLISH_PENDING_EVENTS}", havingValue = "true")
@Service
class EventNotificationService(
  private val topicService: HmppsQueueService,
  private val queueService: QueueService,
  private val objectMapper: ObjectMapper,
  private val authorisationConfig: AuthorisationConfig,
  private val featureFlagConfig: FeatureFlagConfig,
  private val telemetryService: TelemetryService,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun sendEvent(event: EventNotification) {
    if (featureFlagConfig.isNotDisabled(FeatureFlagConfig.DIRECT_SQS_NOTIFICATIONS)) {
      return sendEventToQueue(event)
    }

    return sendEventToTopic(event)
  }

  fun sendEventToTopic(payload: EventNotification) {
    val hmppsEventTopic = topicService.findByTopicId(INTEGRATION_EVENT_TOPIC)
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
          queueService.sendMessageToQueue(objectMapper.writeValueAsString(message), queueName)
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
    // Prison Id check
    val prisonIds = authorisationConfig.allFilters(consumer)?.prisons
    val prisonCheck = prisonIds == null || (event.prisonId != null && prisonIds.contains(event.prisonId))
    // Supervision Status check
    val supervisionStatuses = authorisationConfig.allFilters(consumer)?.supervisionStatuses
    val supervisionStatusCheck = supervisionStatuses == null || (event.metadata?.supervisionStatus != null && supervisionStatuses.contains(event.metadata.supervisionStatus))

    // Log custom event
    if (event.metadata?.supervisionStatus?.contains(SupervisionStatus.PRISONS.name) == true) {
      telemetryService.trackEvent(
        "prisonSupervisionStatusEvents",
        mapOf(
          "consumer" to consumer,
          "prisonId" to event.prisonId,
          "eventType" to event.eventType,
        ),
      )
    }

    // Log supervisionStatusCheck info
    val messageStatus = event.metadata?.supervisionStatus ?: "not defined"
    val consumerStatuses = supervisionStatuses?.joinToString(",") ?: "not defined"
    log.info("Supervision status on message is $messageStatus and the status for consumer $consumer is $consumerStatuses. The Supervision status applicability check returns: $supervisionStatusCheck")

    return consumerEvents.contains(event.eventType) && prisonCheck && supervisionStatusCheck
  }
}
