package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.listeners

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.DomainEventName
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.IntegrationEventType
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.helpers.DomainEvents

class AdjudicationEventTest : uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.listeners.DomainEventsListenerTestCase() {
  private val nomsNumber = "A1234BC"

  @ParameterizedTest
  @ValueSource(
    strings = [
      DomainEventName.Adjudication.Hearing.CREATED,
      DomainEventName.Adjudication.Hearing.DELETED,
      DomainEventName.Adjudication.Hearing.COMPLETED,
      DomainEventName.Adjudication.Punishments.CREATED,
      DomainEventName.Adjudication.Report.CREATED,
    ],
  )
  fun `will process an adjudication notification`(eventType: String) {
    // Arrange
    val message =
      """
      {
        "eventType": "$eventType",
        "version": "1.0",
        "description": "An adjudication has been created:  MDI-000169",
        "occurredAt": "2024-08-14T12:33:34+01:00",
        "additionalInformation": {
          "prisonerNumber": "$nomsNumber"
        }
      }
      """.trimIndent().replace("\n", "")

    val payload =
      DomainEvents
        .generateDomainEvent(eventType, message.replace("\"", "\\\""))

    onDomainEventShouldCreateEventNotification(
      hmppsEventRawMessage = payload,
      hmppsId = nomsNumber,
      expectedNotificationType = IntegrationEventType.PERSON_REPORTED_ADJUDICATIONS_CHANGED.toString(),
    )
  }
}
