package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.listeners

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.enums.IntegrationEventType
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.helpers.DomainEvents
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.helpers.DomainEvents.crn

class AddressChangedEventTest : DomainEventsListenerTestCase() {
  @ParameterizedTest
  @ValueSource(
    strings = [
      "probation-case.address.created",
      "probation-case.address.updated",
      "probation-case.address.deleted",
    ],
  )
  fun `will process an address changed notification`(eventType: String) {
    // Arrange
    val message =
      """
      {"eventType":"$eventType","version": 1,"description":"HMPPS Domain Event","detailUrl":"https://some-url.gov.uk","occurredAt": "2024-08-14T12:33:34+01:00","personReference":{"identifiers":[{"type": "CRN", "value": "$crn"}]}}
      """.trimIndent()

    val payload =
      DomainEvents
        .generateDomainEvent(eventType, message.replace("\"", "\\\""))

    // Act, Assert
    onDomainEventShouldCreateEventNotification(
      hmppsEventRawMessage = payload,
      hmppsId = crn,
      expectedNotificationType = IntegrationEventType.PERSON_ADDRESS_CHANGED.toString(),
    )
  }
}
