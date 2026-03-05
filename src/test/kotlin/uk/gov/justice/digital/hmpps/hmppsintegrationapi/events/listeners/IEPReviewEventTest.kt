package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.listeners

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.DomainEventName
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.IntegrationEventType
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.helpers.DomainEvents

class IEPReviewEventTest : uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.listeners.DomainEventsListenerTestCase() {
  private val nomsNumber = "A1234BC"

  @ParameterizedTest
  @ValueSource(
    strings = [
      DomainEventName.Incentives.IEPReview.INSERTED,
      DomainEventName.Incentives.IEPReview.UPDATED,
      DomainEventName.Incentives.IEPReview.DELETED,
    ],
  )
  fun `will process an incentive review notification`(eventType: String) {
    val message =
      """
      {
        "eventType": "$eventType",
        "version": "1.0",
        "description": "An IEP review has been changed",
        "occurredAt": "2024-08-14T12:33:34+01:00",
        "additionalInformation": {
          "nomsNumber": "$nomsNumber"
        }
      }
      """.trimIndent().replace("\n", "")

    val payload = _root_ide_package_.uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.helpers.DomainEvents.generateDomainEvent(eventType, message.replace("\"", "\\\""))

    // Act, Assert
    onDomainEventShouldCreateEventNotification(
      hmppsEventRawMessage = payload,
      hmppsId = nomsNumber,
      expectedNotificationType = IntegrationEventType.PERSON_IEP_LEVEL_CHANGED.toString(),
    )
  }
}
