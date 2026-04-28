package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.listeners

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.enums.IntegrationEventType
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.helpers.DomainEvents
import java.util.stream.Stream

class DomainEventsListenerPNDAlertsTest : DomainEventsListenerTestCase() {
  private val nomsNumber = "A1234BC"

  @BeforeEach
  internal fun setupAlertTest() {
    assumeIdentities(hmppsId = nomsNumber)
  }

  @ParameterizedTest
  @ArgumentsSource(AlertCodeArgumentSource::class)
  fun `will process and save a pnd alert for person alert created event`(alertCode: String) =
    onDomainEventShouldCreateEventNotificationAlert(
      eventType = "person.alert.created",
      alertCode = alertCode,
      description = "An alert has been created in the alerts service",
    )

  @ParameterizedTest
  @ArgumentsSource(AlertCodeArgumentSource::class)
  fun `will process and save a pnd alert for person alert changed event`(alertCode: String) =
    onDomainEventShouldCreateEventNotificationAlert(
      eventType = "person.alert.changed",
      alertCode = alertCode,
      description = "The alerts for a given person have been updated in the alerts service",
    )

  @ParameterizedTest
  @ArgumentsSource(AlertCodeArgumentSource::class)
  fun `will process and save a pnd alert for person alert deleted event`(alertCode: String) =
    onDomainEventShouldCreateEventNotificationAlert(
      eventType = "person.alert.deleted",
      alertCode = alertCode,
      description = "An alert has been deleted in the alerts service",
    )

  @ParameterizedTest
  @ArgumentsSource(AlertCodeArgumentSource::class)
  fun `will process and save a pnd alert for person alert updated event`(alertCode: String) =
    onDomainEventShouldCreateEventNotificationAlert(
      eventType = "person.alert.updated",
      alertCode = alertCode,
      description = "An alert has been updated in the alerts service",
    )

  private fun onDomainEventShouldCreateEventNotificationAlert(
    eventType: String,
    alertCode: String,
    description: String = "An alert has been created in the alerts service",
  ) {
    // Arrange
    val message =
      """
      {\"eventType\":\"$eventType\",\"additionalInformation\":{\"alertUuid\":\"8339dd96-4a02-4d5b-bc78-4eda22f678fa\",\"alertCode\":\"$alertCode\",\"source\":\"NOMIS\"},\"version\":1,\"description\":\"$description\",\"occurredAt\":\"2024-08-12T19:48:12.771347283+01:00\",\"detailUrl\":\"https://alerts-api.hmpps.service.justice.gov.uk/alerts/8339dd96-4a02-4d5b-bc78-4eda22f678fa\",\"personReference\":{\"identifiers\":[{\"type\":\"NOMS\",\"value\":\"$nomsNumber\"}]}}
      """.trimIndent()

    val payload =
      DomainEvents
        .generateDomainEvent(eventType, message)

    // Act, Assert
    onDomainEventShouldCreateEventNotifications(
      hmppsEventRawMessage = payload,
      IntegrationEventType.PERSON_PND_ALERTS_CHANGED.toString(),
      IntegrationEventType.PERSON_ALERTS_CHANGED.toString(),
    )
  }

  private class AlertCodeArgumentSource : ArgumentsProvider {
    private val alertCodes =
      arrayOf(
        "BECTER",
        "HA",
        "XA",
        "XCA",
        "XEL",
        "XELH",
        "XER",
        "XHT",
        "XILLENT",
        "XIS",
        "XR",
        "XRF",
        "XSA",
        "HA2",
        "RCS",
        "RDV",
        "RKC",
        "RPB",
        "RPC",
        "RSS",
        "RST",
        "RDP",
        "REG",
        "RLG",
        "ROP",
        "RRV",
        "RTP",
        "RYP",
        "HS",
        "SC",
      )

    override fun provideArguments(context: ExtensionContext): Stream<out Arguments> = alertCodes.map { Arguments.of(it) }.stream()
  }
}
