package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.listeners

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.IntegrationEventType
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.helpers.DomainEvents
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.helpers.DomainEvents.crn

class ResponsibleOfficerChangedEventTest : uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.listeners.DomainEventsListenerTestCase() {
  @ParameterizedTest
  @ValueSource(
    strings = [
      "person.community.manager.allocated",
      "person.community.manager.transferred",
      "probation.staff.updated",
    ],
  )
  fun `will process a responsible officer changed notification`(eventType: String) {
    // Arrange
    val hmppsId = crn
    val message =
      """
      {"eventType":"$eventType","version": 1,"description":"HMPPS Domain Event","detailUrl":"https://some-url.gov.uk","occurredAt": "2024-08-14T12:33:34+01:00","personReference":{"identifiers":[{"type": "CRN", "value": "$crn"}]}}
      """.trimIndent()

    val payload = _root_ide_package_.uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.helpers.DomainEvents.generateDomainEvent(eventType, message.replace("\"", "\\\""))

    // Act, Assert
    onDomainEventShouldCreateEventNotification(
      hmppsEventRawMessage = payload,
      hmppsId = hmppsId,
      expectedNotificationType = IntegrationEventType.PERSON_RESPONSIBLE_OFFICER_CHANGED.toString(),
    )
  }
}
