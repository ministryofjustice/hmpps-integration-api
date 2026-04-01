package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.events.listener

import io.kotest.assertions.json.shouldContainJsonKeyValue
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotBeEmpty
import org.awaitility.Awaitility
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.EventNotification
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.helpers.SqsNotificationGeneratingHelper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.events.IntegrationTestWithEventsQueueBase
import java.util.concurrent.TimeUnit

class DomainEventFiltersIntegrationTest : IntegrationTestWithEventsQueueBase() {
  @BeforeEach
  fun setup() {
    eventNotificationRepository.deleteAll()
  }

  @Test
  fun `will create a message containing supervision status of PRISONS`() {
    val rawMessage =
      SqsNotificationGeneratingHelper().generateRawHmppsDomainEvent(
        identifiers = "[{\\\"type\\\":\\\"CRN\\\",\\\"value\\\":\\\"A123123\\\"},{\\\"type\\\":\\\"NOMS\\\",\\\"value\\\":\\\"A3646EA\\\"}]",
      )
    sendDomainSqsMessage(rawMessage)

    Awaitility.await().timeout(1000, TimeUnit.SECONDS).until {
      eventNotificationRepository.findAll().isNotEmpty()
    }
    val savedEvents: List<EventNotification> = eventNotificationRepository.findByHmppsIdIsIn(listOf("A123123"))
    savedEvents.shouldNotBeEmpty().shouldHaveSize(1)
    val eventJson = objectMapper.writeValueAsString(savedEvents.first())

    eventJson.shouldContainJsonKeyValue("$.supervisionStatus", "PRISONS")
  }

  @Test
  fun `will create a message containing supervision status of PROBATION`() {
    val rawMessage =
      SqsNotificationGeneratingHelper().generateRawHmppsDomainEvent(
        identifiers = "[{\\\"type\\\":\\\"CRN\\\",\\\"value\\\":\\\"A123123\\\"},{\\\"type\\\":\\\"NOMS\\\",\\\"value\\\":\\\"A3646EB\\\"}]",
      )
    sendDomainSqsMessage(rawMessage)

    Awaitility.await().until {
      eventNotificationRepository.findAll().isNotEmpty()
    }
    val savedEvents: List<EventNotification> = eventNotificationRepository.findByHmppsIdIsIn(listOf("A123123"))
    savedEvents.shouldNotBeEmpty().shouldHaveSize(1)

    val eventJson = objectMapper.writeValueAsString(savedEvents.first())
    eventJson.shouldContainJsonKeyValue("$.supervisionStatus", "PROBATION")
  }
}
