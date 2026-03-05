package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.helpers.SqsNotificationGeneratingHelper

class DomainEventPredicatesTests {
  @Test
  fun `is not a valid contact event - no additional information`() {
    val event = SqsNotificationGeneratingHelper().createHmppsDomainEvent().copy(additionalInformation = null)
    assertFalse(event.isValidContactEvent())
  }

  @Test
  fun `is not a valid contact event - no mappa category`() {
    val event = SqsNotificationGeneratingHelper().createHmppsDomainEvent().copy(additionalInformation = AdditionalInformation())
    assertFalse(event.isValidContactEvent())
  }

  @Test
  fun `is not a valid contact event - not valid mappa category`() {
    val event = SqsNotificationGeneratingHelper().createHmppsDomainEvent().copy(additionalInformation = AdditionalInformation(mappa = Mappa(999)))
    assertFalse(event.isValidContactEvent())
  }

  @Test
  fun `is a valid contact event`() {
    val event = SqsNotificationGeneratingHelper().createHmppsDomainEvent().copy(additionalInformation = AdditionalInformation(mappa = Mappa(1)))
    assertTrue(event.isValidContactEvent())
  }
}
