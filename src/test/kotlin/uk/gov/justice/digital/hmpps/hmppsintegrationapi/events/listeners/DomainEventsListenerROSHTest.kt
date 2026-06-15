package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.listeners

import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.enums.IntegrationEventType
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.helpers.DomainEvents
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.helpers.DomainEvents.ASSESSMENT_SUMMARY_PRODUCED

class DomainEventsListenerROSHTest : DomainEventsListenerTestCase() {
  private val crn = "X777776"

  @Test
  fun `will process and save a rosh notification with event type RISK_OF_SERIOUS_HARM_CHANGED`() {
    // Arrange
    val eventType = "assessment.summary.produced"
    val message = ASSESSMENT_SUMMARY_PRODUCED

    val payload =
      DomainEvents
        .generateDomainEvent(eventType, message)

    // Act, Assert
    onDomainEventShouldCreateEventNotification(
      hmppsEventRawMessage = payload,
      hmppsId = crn,
      expectedNotificationType = IntegrationEventType.RISK_OF_SERIOUS_HARM_CHANGED.toString(),
    )
  }

  @Test
  fun `will process and save a rosh notification with event type ASSESSMENT_SUMMARY_CHANGE`() {
    // Arrange
    val eventType = "assessment.summary.produced"
    val message = ASSESSMENT_SUMMARY_PRODUCED

    val payload =
      DomainEvents
        .generateDomainEvent(eventType, message)

    // Act, Assert
    onDomainEventShouldCreateEventNotification(
      hmppsEventRawMessage = payload,
      hmppsId = crn,
      expectedNotificationType = IntegrationEventType.ASSESSMENT_SUMMARY_CHANGE.toString(),
    )
  }
}
