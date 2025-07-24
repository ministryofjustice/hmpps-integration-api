package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.sns.model.PublishRequest
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.HmppsTopic
import uk.gov.justice.hmpps.sqs.eventTypeMessageAttributes
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

private val log = KotlinLogging.logger {}

@Component
class DomainEventPublisher(
  private val hmppsQueueService: HmppsQueueService,
  private val objectMapper: ObjectMapper,
) {
  internal val eventTopic by lazy { hmppsQueueService.findByTopicId("domainevents") as HmppsTopic }

  private fun publishEvent(event: HmppsDomainEvent) {
    log.info("Publishing event of type ${event.eventType} for person reference ${event.personReference.identifiers}")
    eventTopic.snsClient
      .publish(
        PublishRequest
          .builder()
          .topicArn(eventTopic.arn)
          .message(objectMapper.writeValueAsString(event))
          .eventTypeMessageAttributes(event.eventType)
          .build()
          .also { log.info("Published event $event to outbound topic") },
      ).get()
  }

  fun createAndPublishEvent(
    prisonNumber: String,
    occurredAt: Instant,
    eventType: String,
    description: String,
    detailUrl: String,
    additionalInformation: Map<String, Any?>,
  ) {
    val event =
      HmppsDomainEvent(
        eventType = eventType,
        description = description,
        detailUrl = detailUrl,
        occurredAt = occurredAt.atZone(ZoneId.of("Europe/London")).toLocalDateTime(),
        personReference = PersonReference(identifiers = listOf(Identifier("NOMS", prisonNumber))),
        additionalInformation = additionalInformation,
      )
    publishEvent(event)
  }

  data class HmppsDomainEvent(
    val version: Int = 1,
    val eventType: String,
    val description: String,
    val detailUrl: String,
    val occurredAt: LocalDateTime,
    val personReference: PersonReference,
    val additionalInformation: Map<String, Any?> = emptyMap(),
  )

  data class PersonReference(
    val identifiers: List<Identifier>,
  )

  data class Identifier(
    val type: String,
    val value: String,
  )
}
