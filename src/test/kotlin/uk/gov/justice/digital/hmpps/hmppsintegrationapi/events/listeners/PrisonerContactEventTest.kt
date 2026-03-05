package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.listeners

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.enums.IntegrationEventType
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.helpers.DomainEvents
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.models.DomainEventName

class PrisonerContactEventTest : DomainEventsListenerTestCase() {
  private val nomsNumber = "A1234BC"
  private val hmppsId = nomsNumber

  @ParameterizedTest
  @ValueSource(
    strings = [
      DomainEventName.PrisonOffenderEvents.Prisoner.CONTACT_ADDED,
      DomainEventName.PrisonOffenderEvents.Prisoner.CONTACT_APPROVED,
      DomainEventName.PrisonOffenderEvents.Prisoner.CONTACT_UNAPPROVED,
      DomainEventName.PrisonOffenderEvents.Prisoner.CONTACT_REMOVED,
    ],
  )
  fun `will process an prisoner contact notification`(eventType: String) {
    // Arrange
    val message =
      """
      {
        "eventType": "$eventType",
        "version": 1,
        "description": "A contact has been added to a prisoner",
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

    val payload =
      DomainEvents
        .generateDomainEvent(eventType, message.replace("\"", "\\\""))

    // Act, Assert
    onDomainEventShouldCreateEventNotification(
      hmppsEventRawMessage = payload,
      hmppsId = hmppsId,
      expectedNotificationType = IntegrationEventType.PERSON_CONTACTS_CHANGED.toString(),
    )
  }
}
