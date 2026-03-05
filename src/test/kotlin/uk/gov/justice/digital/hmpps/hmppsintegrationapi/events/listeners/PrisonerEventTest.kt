package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.listeners

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.DomainEventName
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.IntegrationEventType
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.helpers.DomainEvents

class PrisonerEventTest : uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.listeners.DomainEventsListenerTestCase() {
  private val nomsNumber = "A1234BC"

  @BeforeEach
  internal fun setupPrisonerTest() {
    assumeIdentities(hmppsId = nomsNumber, prisonId = "MDI")
  }

  @ParameterizedTest
  @ValueSource(
    strings = [
      DomainEventName.PrisonerOffenderSearch.Prisoner.CREATED,
      DomainEventName.PrisonerOffenderSearch.Prisoner.UPDATED,
      DomainEventName.PrisonerOffenderSearch.Prisoner.RECEIVED,
    ],
  )
  fun `will process prisoner events`(eventType: String) {
    // Arrange
    val message =
      """
      {
        "eventType": "$eventType",
        "version": "1.0",
        "description": "This is when a prisoner index record has been updated.",
        "occurredAt": "2024-08-14T12:33:34+01:00",
        "additionalInformation": {
          "categoriesChanged": []
        },
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
    onDomainEventShouldCreateEventNotifications(
      hmppsEventRawMessage = payload,
      IntegrationEventType.PERSON_STATUS_CHANGED.toString(),
      IntegrationEventType.PRISONER_CHANGED.toString(),
      IntegrationEventType.PRISONERS_CHANGED.toString(),
    )
  }

  @ParameterizedTest
  @ValueSource(
    strings = [
      DomainEventName.PrisonerOffenderSearch.Prisoner.CREATED,
      DomainEventName.PrisonerOffenderSearch.Prisoner.RECEIVED,
    ],
  )
  fun `will process new prisoner events`(eventType: String) {
    // Arrange
    val message =
      """
      {
        "eventType": "$eventType",
        "version": "1.0",
        "description": "This is when a prisoner index record has been updated.",
        "occurredAt": "2024-08-14T12:33:34+01:00",
        "additionalInformation": {
          "categoriesChanged": []
        },
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
    onDomainEventShouldCreateEventNotifications(
      hmppsEventRawMessage = payload,
      IntegrationEventType.PERSON_STATUS_CHANGED.toString(),
      IntegrationEventType.PRISONER_CHANGED.toString(),
      IntegrationEventType.PRISONERS_CHANGED.toString(),
      IntegrationEventType.PRISONER_NON_ASSOCIATIONS_CHANGED.toString(),
    )
  }

  @Test
  fun `will process an prisoner personal details changed event`() {
    // Arrange
    val eventType = DomainEventName.PrisonerOffenderSearch.Prisoner.UPDATED
    val message =
      """
      {
        "eventType": "$eventType",
        "version": "1.0",
        "description": "This is when a prisoner index record has been updated.",
        "occurredAt": "2024-08-14T12:33:34+01:00",
        "additionalInformation": {
          "categoriesChanged": ["PERSONAL_DETAILS"]
        },
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
    onDomainEventShouldCreateEventNotifications(
      hmppsEventRawMessage = payload,
      IntegrationEventType.PERSON_STATUS_CHANGED.toString(),
      IntegrationEventType.PERSON_NAME_CHANGED.toString(),
    )
  }

  @Test
  fun `will process an prisoner sentence changed event`() {
    // Arrange
    val eventType = DomainEventName.PrisonerOffenderSearch.Prisoner.UPDATED
    val message =
      """
      {
        "eventType": "$eventType",
        "version": "1.0",
        "description": "This is when a prisoner index record has been updated.",
        "occurredAt": "2024-08-14T12:33:34+01:00",
        "additionalInformation": {
          "categoriesChanged": ["SENTENCE"]
        },
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
    onDomainEventShouldCreateEventNotifications(
      hmppsEventRawMessage = payload,
      IntegrationEventType.PERSON_STATUS_CHANGED.toString(),
      IntegrationEventType.PERSON_SENTENCES_CHANGED.toString(),
    )
  }

  @Test
  fun `will process an prisoner location changed notification`() {
    // Arrange
    val eventType = DomainEventName.PrisonerOffenderSearch.Prisoner.UPDATED
    val message =
      """
      {
        "eventType": "$eventType",
        "version": "1.0",
        "description": "This is when a prisoner index record has been updated.",
        "occurredAt": "2024-08-14T12:33:34+01:00",
        "additionalInformation": {
          "categoriesChanged": ["LOCATION"]
        },
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
    onDomainEventShouldCreateEventNotifications(
      hmppsEventRawMessage = payload,
      IntegrationEventType.PERSON_STATUS_CHANGED.toString(),
      IntegrationEventType.PERSON_CELL_LOCATION_CHANGED.toString(),
    )
  }

  @Test
  fun `will process an prisoner physical details changed notification`() {
    val eventType = DomainEventName.PrisonerOffenderSearch.Prisoner.UPDATED
    val message =
      """
      {
        "eventType": "$eventType",
        "version": "1.0",
        "description": "This is when a prisoner index record has been updated.",
        "occurredAt": "2024-08-14T12:33:34+01:00",
        "additionalInformation": {
          "categoriesChanged": ["PHYSICAL_DETAILS"]
        },
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
    onDomainEventShouldCreateEventNotifications(
      hmppsEventRawMessage = payload,
      IntegrationEventType.PERSON_STATUS_CHANGED.toString(),
      IntegrationEventType.PERSON_PHYSICAL_CHARACTERISTICS_CHANGED.toString(),
    )
  }
}
