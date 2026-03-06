package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.listeners

import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.enums.IntegrationEventType
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.helpers.DomainEvents

class DomainEventsListenerSANReviewUpdatedTest : DomainEventsListenerTestCase() {
  private val nomsNumber = "A1234BC"

  private val eventType = "san.review-schedule.updated"

  @Test
  fun `will process a san review schedule updated notification`() {
    // Arrange
    val message =
      """
      { \"eventType\": \"$eventType\",  \"description\": \"A Support for additional needs review schedule was created or amended\",  \"detailUrl\": \"http://localhost:8080/profile/$nomsNumber/reviews/review-schedules\",  \"occurredAt\": \"2024-08-08T09:07:55\",  \"personReference\": {    \"identifiers\": [      {        \"type\": \"NOMS\",        \"value\": \"$nomsNumber\"      }    ]  }}
      """.trimIndent()

    val payload =
      DomainEvents
        .generateDomainEvent(eventType, message)

    // Act, Assert
    onDomainEventShouldCreateEventNotification(
      hmppsEventRawMessage = payload,
      hmppsId = nomsNumber,
      expectedNotificationType = IntegrationEventType.SAN_REVIEW_SCHEDULE_CHANGED.toString(),
    )
  }
}
