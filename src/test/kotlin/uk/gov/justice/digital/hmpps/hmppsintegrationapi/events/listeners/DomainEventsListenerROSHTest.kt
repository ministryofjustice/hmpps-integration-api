package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.listeners

import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.IntegrationEventType
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.helpers.DomainEvents
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.helpers.DomainEvents.ASSESSMENT_SUMMARY_PRODUCED

class DomainEventsListenerROSHTest : uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.listeners.DomainEventsListenerTestCase() {
  private val crn = "X777776"

  @Test
  fun `will process and save a rosh notification`() {
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
}
