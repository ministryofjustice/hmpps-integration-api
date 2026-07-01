package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.listeners

import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.enums.IntegrationEventType
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.helpers.DomainEvents
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.helpers.DomainEvents.ASSESSMENT_SUMMARY_PRODUCED
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.helpers.DomainEvents.ASSESSMENT_SUMMARY_PRODUCED_LOCKED_INCOMPLETE

class DomainEventsListenerAssessmentSummaryTest : DomainEventsListenerTestCase() {
  private val crn = "X777776"

  @Test
  fun `will process and save a assessment summary notification with the correct status`() {
    // Arrange
    val eventType = "assessment.summary.produced"
    val message = ASSESSMENT_SUMMARY_PRODUCED
    assumeIdentities(hmppsId = crn)

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

  @Test
  fun `will NOT create a assessment summary notification when incorrect status`() {
    // Arrange
    val eventType = "assessment.summary.produced"
    val message = ASSESSMENT_SUMMARY_PRODUCED_LOCKED_INCOMPLETE
    assumeIdentities(hmppsId = crn)

    val payload =
      DomainEvents
        .generateDomainEvent(eventType, message)

    onDomainEventShouldNotCreateEventNotification(payload)
  }
}
