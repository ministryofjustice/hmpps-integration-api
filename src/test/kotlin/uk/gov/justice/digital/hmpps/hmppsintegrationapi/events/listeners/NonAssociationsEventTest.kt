package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.listeners

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.DomainEventName
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.IntegrationEventType
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.helpers.DomainEvents

class NonAssociationsEventTest : uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.listeners.DomainEventsListenerTestCase() {
  private val nomsNumber = "A1234BC"

  @BeforeEach
  internal fun setupLocationTest() {
    assumeIdentities(hmppsId = nomsNumber, prisonId = "MDI")
  }

  @ParameterizedTest
  @ValueSource(
    strings = [
      DomainEventName.PrisonOffenderEvents.Prisoner.NonAssociationDetail.CHANGED,
    ],
  )
  fun `will process an non-association notification`(eventType: String) {
    // Arrange
    val message =
      """
      {
        "eventType": "$eventType",
        "version": "1.0",
        "description": "A prisoner non-association detail record has changed",
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
      expectedNotificationType = IntegrationEventType.PRISONER_NON_ASSOCIATIONS_CHANGED.toString(),
    )
  }
}
