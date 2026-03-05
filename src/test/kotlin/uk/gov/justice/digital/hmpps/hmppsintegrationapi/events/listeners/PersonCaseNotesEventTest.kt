package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.listeners

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.DomainEventName
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.IntegrationEventType
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.helpers.DomainEvents

class PersonCaseNotesEventTest : uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.listeners.DomainEventsListenerTestCase() {
  private val nomsNumber = "A1234BC"
  private val hmppsId = nomsNumber

  @ParameterizedTest
  @ValueSource(
    strings = [
      DomainEventName.Person.CaseNote.CREATED,
      DomainEventName.Person.CaseNote.UPDATED,
      DomainEventName.Person.CaseNote.DELETED,
    ],
  )
  fun `will process an case note notification`(eventType: String) {
    // Arrange
    val message =
      """
      {
        "eventType": "$eventType",
        "version": "1.0",
        "description": "A case note has been created for a person",
        "occurredAt": "2024-08-14T12:33:34+01:00",
        "personReference": {
          "identifiers": [
            {
              "type": "NOMS",
              "value": "$nomsNumber"
             }
          ]
        }
      }
      """.trimIndent().replace("\n", "")

    val payload = _root_ide_package_.uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.helpers.DomainEvents.generateDomainEvent(eventType, message.replace("\"", "\\\""))

    // Act, Assert
    onDomainEventShouldCreateEventNotification(
      hmppsEventRawMessage = payload,
      hmppsId = hmppsId,
      expectedNotificationType = IntegrationEventType.PERSON_CASE_NOTES_CHANGED.toString(),
    )
  }
}
