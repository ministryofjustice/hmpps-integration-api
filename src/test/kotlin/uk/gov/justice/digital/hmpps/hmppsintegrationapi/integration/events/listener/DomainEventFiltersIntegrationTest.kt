package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.events.listener

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.awaitility.Awaitility
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.test.context.TestPropertySource
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.EventNotification
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.helpers.SqsNotificationGeneratingHelper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.events.IntegrationTestWithEventsQueueBase
import kotlin.test.assertEquals

class DomainEventFiltersIntegrationTest : IntegrationTestWithEventsQueueBase() {
  @BeforeEach
  fun setup() {
    eventNotificationRepository.deleteAll()
  }

  @Test
  fun `will create a message containing supervision status of PRISONS when active in prison nomis in message`() {
    val rawMessage =
      SqsNotificationGeneratingHelper().generateRawHmppsDomainEvent(
        identifiers = "[{\\\"type\\\":\\\"CRN\\\",\\\"value\\\":\\\"$crnNotActiveInProbation\\\"},{\\\"type\\\":\\\"NOMS\\\",\\\"value\\\":\\\"$nomsIdActiveInPrison\\\"}]",
      )
    sendDomainSqsMessage(rawMessage)

    Awaitility.await().untilAsserted {
      eventNotificationRepository.findAll().isNotEmpty()
      val savedEvents: List<EventNotification> = eventNotificationRepository.findByHmppsIdIsIn(listOf(crnNotActiveInProbation))
      savedEvents.shouldNotBeEmpty().shouldHaveSize(1)
      assertEquals("PRISONS", savedEvents.first().filters?.supervisionStatus)
    }
  }

  @Test
  fun `will create a message containing supervision status of PROBATION when inactive prison status nomis in message`() {
    val rawMessage =
      SqsNotificationGeneratingHelper().generateRawHmppsDomainEvent(
        identifiers = "[{\\\"type\\\":\\\"CRN\\\",\\\"value\\\":\\\"$crnActiveInProbation\\\"},{\\\"type\\\":\\\"NOMS\\\",\\\"value\\\":\\\"$nomsIdNotActiveInPrison\\\"}]",
      )
    sendDomainSqsMessage(rawMessage)

    Awaitility.await().untilAsserted {
      eventNotificationRepository.findAll().isNotEmpty()
      val savedEvents: List<EventNotification> = eventNotificationRepository.findByHmppsIdIsIn(listOf(crnActiveInProbation))
      savedEvents.shouldNotBeEmpty().shouldHaveSize(1)
      assertEquals("PROBATION", savedEvents.first().filters?.supervisionStatus)
    }
  }

  @Test
  fun `will create a message containing supervision status of PROBATION when CRN only is in message`() {
    val rawMessage =
      SqsNotificationGeneratingHelper().generateRawHmppsDomainEvent(
        identifiers = "[{\\\"type\\\":\\\"CRN\\\",\\\"value\\\":\\\"$crnActiveInProbation\\\"}]",
      )
    sendDomainSqsMessage(rawMessage)

    Awaitility.await().untilAsserted {
      eventNotificationRepository.findAll().isNotEmpty()
      val savedEvents: List<EventNotification> = eventNotificationRepository.findByHmppsIdIsIn(listOf(crnActiveInProbation))
      savedEvents.shouldNotBeEmpty().shouldHaveSize(1)
      assertEquals("PROBATION", savedEvents.first().filters?.supervisionStatus)
    }
  }

  @Test
  fun `will create a message containing supervision status of NONE when neither prison or probation status is set`() {
    val rawMessage =
      SqsNotificationGeneratingHelper().generateRawHmppsDomainEvent(
        identifiers = "[{\\\"type\\\":\\\"CRN\\\",\\\"value\\\":\\\"$crnNotActiveInPrisonOrProb\\\"}]",
      )
    sendDomainSqsMessage(rawMessage)

    Awaitility.await().untilAsserted {
      eventNotificationRepository.findAll().isNotEmpty()
      val savedEvents: List<EventNotification> = eventNotificationRepository.findByHmppsIdIsIn(listOf(crnNotActiveInPrisonOrProb))
      savedEvents.shouldNotBeEmpty().shouldHaveSize(1)
      assertEquals("NONE", savedEvents.first().filters?.supervisionStatus)
    }
  }

  @Test
  fun `will create a message containing supervision status of UNKNOWN when prisoner offender search returns not found`() {
    prisonerOffenderSearchMockServer.stubForGet(
      "/prisoner/$nomsIdActiveInPrison",
      "",
      HttpStatus.NOT_FOUND,
    )

    val rawMessage =
      SqsNotificationGeneratingHelper().generateRawHmppsDomainEvent(
        identifiers = "[{\\\"type\\\":\\\"CRN\\\",\\\"value\\\":\\\"$crnNotActiveInProbation\\\"}]",
      )
    sendDomainSqsMessage(rawMessage)

    Awaitility.await().untilAsserted {
      eventNotificationRepository.findAll().isNotEmpty()
      val savedEvents: List<EventNotification> = eventNotificationRepository.findByHmppsIdIsIn(listOf(crnNotActiveInProbation))
      savedEvents.shouldNotBeEmpty().shouldHaveSize(1)
      assertEquals("UNKNOWN", savedEvents.first().filters?.supervisionStatus)
    }
  }

  @Test
  fun `will create a message containing supervision status of UNKNOWN when unable to resolve to a nomis number`() {
    val rawMessage =
      SqsNotificationGeneratingHelper().generateRawHmppsDomainEvent(
        identifiers = "[{\\\"type\\\":\\\"CRN\\\",\\\"value\\\":\\\"$crnUnknownInPrison\\\"}]",
      )
    sendDomainSqsMessage(rawMessage)

    Awaitility.await().untilAsserted {
      eventNotificationRepository.findAll().isNotEmpty()
      val savedEvents: List<EventNotification> = eventNotificationRepository.findByHmppsIdIsIn(listOf(crnUnknownInPrison))
      savedEvents.shouldNotBeEmpty().shouldHaveSize(1)
      assertEquals("UNKNOWN", savedEvents.first().filters?.supervisionStatus)
    }
  }

  @Test
  fun `will send the event to the DLQ prisoner offender search returns a 500`() {
    // Clear cache
    prisonerOffenderSearchMockServer.stubForGet(
      "/prisoner/$nomsIdNotActiveInPrison",
      "",
      HttpStatus.INTERNAL_SERVER_ERROR,
    )

    val rawMessage =
      SqsNotificationGeneratingHelper().generateRawHmppsDomainEvent(
        identifiers = "[{\\\"type\\\":\\\"CRN\\\",\\\"value\\\":\\\"$crnActiveInProbation\\\"}]",
      )
    sendDomainSqsMessage(rawMessage)

    Awaitility.await().until {
      getNumberOfMessagesCurrentlyOndomainEventsDeadLetterQueue() > 0
    }

    val deadLetterQueueMessage = geMessagesCurrentlyOnDomainEventsDeadLetterQueue()
    val message = deadLetterQueueMessage.messages().first()
    message.body().shouldBe(rawMessage)
  }
}

@TestPropertySource(properties = ["feature-flag.include-supervision-status-attribute=false"])
class WithoutSupervisionStatusFilter : IntegrationTestWithEventsQueueBase() {
  @BeforeEach
  fun setup() {
    eventNotificationRepository.deleteAll()
  }

  @Test
  fun `will create a message without the supervision status if feature flag is not set`() {
    val rawMessage =
      SqsNotificationGeneratingHelper().generateRawHmppsDomainEvent(
        identifiers = "[{\\\"type\\\":\\\"CRN\\\",\\\"value\\\":\\\"$crn\\\"}]",
      )
    sendDomainSqsMessage(rawMessage)

    Awaitility.await().untilAsserted {
      val savedEvents: List<EventNotification> = eventNotificationRepository.findByHmppsIdIsIn(listOf(crn))
      savedEvents.shouldNotBeEmpty().shouldHaveSize(1)
      savedEvents.first().filters.shouldBeNull()
    }
  }
}
