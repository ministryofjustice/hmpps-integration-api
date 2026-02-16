package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.sns.SnsAsyncClient
import software.amazon.awssdk.services.sns.model.MessageAttributeValue
import software.amazon.awssdk.services.sns.model.PublishRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.EventNotification
import uk.gov.justice.hmpps.sqs.HmppsQueueService

@ConditionalOnProperty("feature-flag.enable-send-processed-events", havingValue = "true")
@Service
class IntegrationEventTopicService(
  private val hmppsQueueService: HmppsQueueService,
  private val objectMapper: ObjectMapper,
) {
  private final val hmppsEventsTopicSnsClient: SnsAsyncClient
  private final val topicArn: String

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  init {
    val hmppsEventTopic = hmppsQueueService.findByTopicId("integrationeventtopic")
    topicArn = hmppsEventTopic!!.arn
    hmppsEventsTopicSnsClient = hmppsEventTopic.snsClient
  }

  fun sendEvent(payload: EventNotification) {
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
}
