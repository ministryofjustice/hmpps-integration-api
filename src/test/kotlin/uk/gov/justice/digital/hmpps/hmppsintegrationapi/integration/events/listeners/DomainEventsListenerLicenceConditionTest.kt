package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.events.listeners

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.IntegrationEventType
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.events.helpers.DomainEvents

class DomainEventsListenerLicenceConditionTest : DomainEventsListenerTestCase() {
  private val crn = "X777776"
  private val nomsNumber = "A1234BC"

  @ParameterizedTest
  @CsvSource(
    value = [
      "create-and-vary-a-licence.licence.activated, 99059",
      "create-and-vary-a-licence.licence.inactivated, 90386",
    ],
  )
  fun `will process and save a licence notification`(
    eventType: String,
    licenceId: String,
  ) {
    // Arrange
    val message =
      """
      {\"eventType\":\"$eventType\",\"additionalInformation\":{\"licenceId\":\"$licenceId\"},\"detailUrl\":\"https://create-and-vary-a-licence-api.hmpps.service.justice.gov.uk/public/licences/id/$licenceId\",\"version\":1,\"occurredAt\":\"2024-08-14T16:42:13.725721689+01:00\",\"description\":\"Licence activated for Licence ID $licenceId\",\"personReference\":{\"identifiers\":[{\"type\":\"CRN\",\"value\":\"$crn\"},{\"type\":\"NOMS\",\"value\":\"$nomsNumber\"}]}}
      """.trimIndent()

    val payload = DomainEvents.generateDomainEvent(eventType, message)

    // Act, Assert
    onDomainEventShouldCreateEventNotification(
      hmppsEventRawMessage = payload,
      hmppsId = crn,
      expectedNotificationType = IntegrationEventType.LICENCE_CONDITION_CHANGED.toString(),
    )
  }
}
