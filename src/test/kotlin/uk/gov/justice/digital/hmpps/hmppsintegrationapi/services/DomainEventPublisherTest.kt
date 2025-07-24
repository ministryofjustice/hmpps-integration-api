package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.check
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import software.amazon.awssdk.services.sns.SnsAsyncClient
import software.amazon.awssdk.services.sns.model.MessageAttributeValue
import software.amazon.awssdk.services.sns.model.PublishRequest
import software.amazon.awssdk.services.sns.model.PublishResponse
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.HmppsTopic
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.CompletableFuture.completedFuture

class DomainEventPublisherTest {
  private val hmppsQueueService: HmppsQueueService = mock()
  private val snsClient: SnsAsyncClient = mock()
  private val objectMapper: ObjectMapper = mock()
  private val service = DomainEventPublisher(hmppsQueueService, objectMapper)

  @Test
  fun `createAndPublishEvent builds and sends correct event`() {
    val prisonNumber = "A1234BC"
    val occurredAt = Instant.now()
    val eventType = "test.event.type"
    val description = "Test event"
    val detailUrl = "http://detail.url"
    val additionalInformation = mapOf("clientReference" to "12345")

    whenever(objectMapper.writeValueAsString(any())).thenReturn("eventAsJson")
    whenever(hmppsQueueService.findByTopicId("domainevents")).thenReturn(HmppsTopic("id", "topicArn", snsClient))
    whenever(snsClient.publish(any<PublishRequest>())).thenReturn(completedFuture(PublishResponse.builder().messageId("1").build()))

    service.createAndPublishEvent(prisonNumber, occurredAt, eventType, description, detailUrl, additionalInformation)

    verify(objectMapper).writeValueAsString(
      check<DomainEventPublisher.HmppsDomainEvent> {
        assertThat(it).isEqualTo(
          DomainEventPublisher.HmppsDomainEvent(
            eventType = eventType,
            description = description,
            detailUrl = detailUrl,
            occurredAt = occurredAt.atZone(ZoneId.of("Europe/London")).toLocalDateTime(),
            personReference =
              DomainEventPublisher.PersonReference(
                identifiers =
                  listOf(
                    DomainEventPublisher.Identifier(
                      type = "NOMS",
                      value = prisonNumber,
                    ),
                  ),
              ),
            additionalInformation = additionalInformation,
          ),
        )
      },
    )
  }

  @Test
  fun `createAndPublishEvent sends event to SNS client`() {
    val occurredAt = Instant.now()
    val eventType = "test.event.type"
    val detailUrl = "http://detail.url"

    whenever(objectMapper.writeValueAsString(any())).thenReturn("eventAsJson")
    whenever(hmppsQueueService.findByTopicId("domainevents")).thenReturn(HmppsTopic("id", "topicArn", snsClient))
    whenever(snsClient.publish(any<PublishRequest>())).thenReturn(completedFuture(PublishResponse.builder().messageId("1").build()))

    service.createAndPublishEvent("A1234BC", occurredAt, eventType, "Test event", detailUrl, mapOf("clientReference" to "12345"))

    verify(snsClient).publish(
      PublishRequest
        .builder()
        .message("eventAsJson")
        .topicArn("topicArn")
        .messageAttributes(
          mapOf(
            "eventType" to
              MessageAttributeValue
                .builder()
                .dataType("String")
                .stringValue(eventType)
                .build(),
          ),
        ).build(),
    )
  }
}
