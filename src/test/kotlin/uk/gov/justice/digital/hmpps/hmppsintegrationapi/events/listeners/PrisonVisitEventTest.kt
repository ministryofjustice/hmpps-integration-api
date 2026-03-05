package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.listeners

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.DomainEventName
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.IntegrationEventType
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.helpers.DomainEvents

class PrisonVisitEventTest : uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.listeners.DomainEventsListenerTestCase() {
  private val nomsNumber = "A1234BC"

  @BeforeEach
  internal fun setupVisitTest() {
    assumeIdentities(hmppsId = nomsNumber, prisonId = "MDI")
  }

  @ParameterizedTest
  @ValueSource(
    strings = [
      DomainEventName.PrisonVisit.BOOKED,
      DomainEventName.PrisonVisit.CHANGED,
      DomainEventName.PrisonVisit.CANCELLED,
    ],
  )
  fun `will process an visit changed notification`(eventType: String) {
    // Arrange
    val message =
      """
      {
        "eventType": "$eventType",
        "version": "1.0",
        "description": "Prison visit changed",
        "occurredAt": "2024-08-14T12:33:34+01:00",
        "prisonerId": "$nomsNumber",
        "additionalInformation": {
          "reference": "nx-ce-vq-ry"
        }
      }
      """.trimIndent().replace("\n", "")

    val payload = _root_ide_package_.uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.helpers.DomainEvents.generateDomainEvent(eventType, message.replace("\"", "\\\""))

    // Act, Assert
    onDomainEventShouldCreateEventNotifications(
      hmppsEventRawMessage = payload,
      IntegrationEventType.PERSON_FUTURE_VISITS_CHANGED.toString(),
      IntegrationEventType.PRISON_VISITS_CHANGED.toString(),
      IntegrationEventType.VISIT_CHANGED.toString(),
    )
  }
}
