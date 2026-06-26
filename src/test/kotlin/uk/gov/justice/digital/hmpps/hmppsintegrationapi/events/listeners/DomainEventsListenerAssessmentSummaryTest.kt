package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.listeners

import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.EventNotification
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

    domainEventsListener.onDomainEvent(payload)
    // Assert
    val events = mutableListOf<EventNotification>()
    verify { eventNotificationRepository.insert(capture(events)) }

    assertThat(events.size).isEqualTo(2)
    assertThat(events.map { it.eventType }).contains(IntegrationEventType.RISK_OF_SERIOUS_HARM_CHANGED.name)
    assertThat(events.map { it.eventType }).contains(IntegrationEventType.ASSESSMENT_SUMMARY_CHANGE.name)
  }

  @Test
  fun `will NOT create a assessment summary notification when incorrect status - will only create a RISK_OF_SERIOUS_HARM_CHANGED`() {
    // Arrange
    val eventType = "assessment.summary.produced"
    val message = ASSESSMENT_SUMMARY_PRODUCED_LOCKED_INCOMPLETE
    assumeIdentities(hmppsId = crn)

    val payload =
      DomainEvents
        .generateDomainEvent(eventType, message)

    domainEventsListener.onDomainEvent(payload)
    // Assert
    val events = mutableListOf<EventNotification>()
    verify { eventNotificationRepository.insert(capture(events)) }

    assertThat(events.size).isEqualTo(1)
    assertThat(events.map { it.eventType }).contains(IntegrationEventType.RISK_OF_SERIOUS_HARM_CHANGED.name)
  }
}
