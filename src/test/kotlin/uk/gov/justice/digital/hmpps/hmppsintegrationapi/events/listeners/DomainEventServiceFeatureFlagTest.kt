package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.listeners

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.config.FeatureFlagTestConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.EventNotification
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.Metadata
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.enums.IntegrationEventType
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.models.HmppsDomainEvent

/**
 * Tests of [uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.services.domain.DeduplicationDomainEventService] with feature flags [FeatureFlagTestConfig]
 *
 * To test around:
 * - IntegrationEventTypes can have a feature flag name associated with them
 *
 * Cases and flags:
 * - IntegrationEventTypes with no feature flag associated are enabled. e.g. `PERSON_STATUS_CHANGED`
 * - IntegrationEventTypes associated with a feature flag set to “true” are enabled, e.g. `PRISONER_MERGED`
 * - IntegrationEventTypes associated with a feature flag set to “false” are not enabled, e.g. `PERSON_LANGUAGES_CHANGED`
 * - IntegrationEventTypes that reference a feature flag that does not exist are disabled, e.g. `PRISONER_BASE_LOCATION_CHANGED`
 *      * and an error is logged with the name of the event and the name of the flag
 */
class DomainEventServiceFeatureFlagTest : DomainEventsListenerTestCase() {
  companion object {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  /**
   *  Feature flags:
   * - `person-languages-changed-notifications-enabled` : false
   * - `prisoner-merge-notifications-enabled` : true
   * - `prisoner-base-location-changed-notifications-enabled` is undefined (not set)
   */
  private val featureFlags =
    mapOf(
      FeatureFlagConfig.PERSON_LANGUAGES_CHANGED_NOTIFICATIONS_ENABLED to false,
      FeatureFlagConfig.PRISONER_MERGED_NOTIFICATIONS_ENABLED to true,
      FeatureFlagConfig.DEDUPLICATE_EVENTS to true,
    )
  private val hmppsId = "AA1234A"

  private val serviceLog = mockk<Logger>()

  val featureFlagConfig get() = featureFlagTestConfig.featureFlagConfig

  @BeforeEach
  internal fun setup() {
    assumeIdentities(hmppsId = hmppsId, prisonId = null)
    // Assuming defined feature flags
    featureFlags.forEach { featureFlagTestConfig.assumeFeatureFlag(it.key, it.value) }

    // service logging
    every { serviceLog.warn(any()) } answers { log.warn(firstArg()) }
    every { serviceLog.error(any<String>(), any(), any()) } answers { log.error(firstArg() as String, secondArg(), thirdArg()) }
  }

  @AfterEach
  internal fun tearDown() {
    featureFlagTestConfig.resetAllFlags()
  }

  // IntegrationEventTypes with no feature flag associated are enabled; e.g. person-status-changed
  @Test
  fun `should process and save an event without feature flag associated`() =
    executeShouldSaveEventNotification(
      hmppsDomainEvent = hmppsDomainEvent("prisoner-offender-search.prisoner.created"),
      generateEventNotification(IntegrationEventType.PERSON_STATUS_CHANGED, "v1/persons/$hmppsId"),
    )

  @Nested
  inner class GivenFeatureFlags {
    // IntegrationEventTypes associated with a feature flag set to “true” are enabled; e.g. prisoner-merged
    @Test
    fun `should process and save an event with feature flag enabled`() {
      // Arrange
      val removedNomisNumber = "AA0001A"
      val updatedNomisNumber = "AA0002A"
      val prisonId = "MDI"
      val hmppsId = updatedNomisNumber

      val hmppsDomainEvent = sqsNotificationHelper.createHmppsMergedDomainEvent(nomisNumber = updatedNomisNumber, removedNomisNumber = removedNomisNumber)

      val expectedEventNotifications =
        listOf(
          generateEventNotification(
            eventType = IntegrationEventType.PRISONER_MERGED,
            urlSuffix = "v1/persons/$removedNomisNumber",
            hmppsId = removedNomisNumber,
            prisonId = prisonId,
          ),
          generateEventNotification(
            eventType = IntegrationEventType.PERSON_STATUS_CHANGED,
            urlSuffix = "v1/persons/$hmppsId",
            hmppsId = hmppsId,
            prisonId = prisonId,
          ),
        )
      assumeIdentities(hmppsId, prisonId)

      // Act, Assert
      executeShouldSaveEventNotifications(hmppsDomainEvent, expectedEventNotifications)
    }

    // IntegrationEventTypes associated with a feature flag set to “false” are not enabled; e.g. person-languages-changed
    // IntegrationEventTypes that are not enabled are not written to the database
    @Test
    fun `should process and save enabled events, and skip event with feature flag disabled`() {
      // Arrange
      val hmppsDomainEvent = hmppsDomainEvent("prisoner-offender-search.prisoner.created", "MDI")
      val unexpectedNotificationTypes =
        arrayOf(
          IntegrationEventType.PERSON_LANGUAGES_CHANGED,
        )
      val expectedNotificationTypes =
        listOf(
          IntegrationEventType.PERSON_STATUS_CHANGED,
          IntegrationEventType.PERSON_ADDRESS_CHANGED,
          IntegrationEventType.PRISONERS_CHANGED,
          IntegrationEventType.PRISONER_CHANGED,
        )

      // Act, Assert (unexpected events)
      executeShouldNotSaveEventNotification(hmppsDomainEvent, *unexpectedNotificationTypes)

      // Assert (expected events)
      expectedNotificationTypes.forEach { expectedNotificationType ->
        verify(exactly = 1) { eventNotificationRepository.insert(match { it.eventType == expectedNotificationType.toString() }) }
      }
    }

    // IntegrationEventTypes that reference a feature flag that does not exist are disabled; e.g. prisoner-base-location-changed
    //    and an error is logged with the name of the event and the name of the flag, e.g. event `PRISONER_BASE_LOCATION_CHANGED` with feature-flag `prisoner-base-location-changed-notifications-enabled`
    // IntegrationEventTypes that are not enabled are not written to the database
    @Test
    fun `should process and NOT save an event with feature flag undefined`() {
      // Arrange
      val hmppsDomainEvent = sqsNotificationHelper.createHmppsPrisonerReceivedDomainEvent()
      val unexpectedEventType = IntegrationEventType.PRISONER_BASE_LOCATION_CHANGED
      featureFlagTestConfig.resetFeatureFlag(FeatureFlagConfig.PRISONER_BASE_LOCATION_CHANGED_NOTIFICATIONS_ENABLED)

      // Act, Assert
      executeShouldNotSaveEventNotification(hmppsDomainEvent, unexpectedEventType)
    }
  }

  // Execute event and verify that no unexpected notification has been saved/persisted
  private fun executeShouldNotSaveEventNotification(
    hmppsDomainEvent: HmppsDomainEvent,
    vararg unexpectedNotificationTypes: IntegrationEventType,
  ) {
    // Act
    deduplicationDomainEventService.execute(hmppsDomainEvent)

    // Assert
    // Verify no unexpected event notifications persisted via repository
    unexpectedNotificationTypes.forEach { unexpectedNotificationType ->
      verify(exactly = 0) { eventNotificationRepository.insert(match { it.eventType == unexpectedNotificationType.toString() }) }
    }
  }

  private fun generateEventNotification(
    eventType: IntegrationEventType,
    urlSuffix: String,
    hmppsId: String = this.hmppsId,
    prisonId: String? = null,
  ) = EventNotification(
    eventType = eventType.toString(),
    url = "$baseUrl/$urlSuffix",
    hmppsId = hmppsId,
    prisonId = prisonId,
    lastModifiedDatetime = currentTime,
    metadata = Metadata(supervisionStatus = "PRISONS"),
  )

  private fun hmppsDomainEvent(domainEventType: String) = sqsNotificationHelper.createHmppsDomainEvent(domainEventType)

  private fun hmppsDomainEvent(
    domainEventType: String,
    prisonId: String,
  ) = sqsNotificationHelper.createHmppsDomainEventWithPrisonId(domainEventType, prisonId = prisonId)
}
