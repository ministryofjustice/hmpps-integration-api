package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.listeners

import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.enums.IntegrationEventType
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.helpers.DomainEvents.generateDomainEvent

class DomainEventsListenerPLPReviewUpdatedTest : DomainEventsListenerTestCase() {
  private val nomsNumber = "A1234BC"

  private val eventType = "plp.review-schedule.updated"

  @Test
  fun `will process a plp review schedule updated notification`() {
    // Arrange
    val message =
      """
      { \"eventType\": \"$eventType\",  \"description\": \"A prisoner learning plan review schedule created or amended\",  \"detailUrl\": \"http://localhost:8080/inductions/$nomsNumber/review-schedule\",  \"occurredAt\": \"2024-08-08T09:07:55\",  \"personReference\": {    \"identifiers\": [      {        \"type\": \"NOMS\",        \"value\": \"$nomsNumber\"      }    ]  }}
      """.trimIndent()

    val payload = generateDomainEvent(eventType, message)

    // Act, Assert
    onDomainEventShouldCreateEventNotification(
      hmppsEventRawMessage = payload,
      hmppsId = nomsNumber,
      expectedNotificationType = IntegrationEventType.PLP_REVIEW_SCHEDULE_CHANGED.toString(),
    )
  }
}
