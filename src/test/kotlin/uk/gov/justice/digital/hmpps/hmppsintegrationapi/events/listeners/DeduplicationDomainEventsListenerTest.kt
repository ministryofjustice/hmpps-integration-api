package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.listeners

import com.fasterxml.jackson.core.JsonParseException
import io.awspring.cloud.sqs.listener.AsyncAdapterBlockingExecutionFailedException
import io.awspring.cloud.sqs.listener.ListenerExecutionFailedException
import io.mockk.Called
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.messaging.support.GenericMessage
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.config.FeatureFlagTestConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.EventNotification
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.enums.IntegrationEventType
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.helpers.SqsNotificationGeneratingHelper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.listener.DomainEventsListener
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.models.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.repository.EventNotificationRepository
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.services.domain.DeduplicationDomainEventService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.services.domain.DomainEventIdentitiesResolver
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.telemetry.TelemetryService
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.CompletionException
import kotlin.getValue

class DeduplicationDomainEventsListenerTest : DomainEventsListenerTestCase() {
  private val crn = "X777776"

  @Test
  fun `when risk-assessment scores determined event is received, it should create event notification RISK_SCORE_CHANGED`() {
    val hmppsEventRawMessage =
      sqsNotificationHelper.generateRawHmppsDomainEventWithoutRegisterType(
        eventType = "risk-assessment.scores.determined",
        messageEventType = "risk-assessment.scores.ogrs.determined",
      )
    val expectedNotificationType = "RISK_SCORE_CHANGED"
    assumeIdentities(hmppsId = crn)

    onDomainEventShouldCreateEventNotification(hmppsEventRawMessage, expectedNotificationType)
  }

  @ParameterizedTest
  @CsvSource(
    "probation-case.registration.added, ASFO, PROBATION_STATUS_CHANGED",
    "probation-case.registration.deleted, ASFO, PROBATION_STATUS_CHANGED",
    "probation-case.registration.deregistered, ASFO, PROBATION_STATUS_CHANGED",
    "probation-case.registration.updated, ASFO, PROBATION_STATUS_CHANGED",
    "probation-case.registration.added, WRSM, PROBATION_STATUS_CHANGED",
    "probation-case.registration.deleted, WRSM, PROBATION_STATUS_CHANGED",
    "probation-case.registration.deregistered, WRSM, PROBATION_STATUS_CHANGED",
    "probation-case.registration.updated, WRSM, PROBATION_STATUS_CHANGED",
    "probation-case.registration.added, RCCO, DYNAMIC_RISKS_CHANGED",
    "probation-case.registration.deleted, RCCO, DYNAMIC_RISKS_CHANGED",
    "probation-case.registration.deregistered, RCCO, DYNAMIC_RISKS_CHANGED",
    "probation-case.registration.updated, RCCO, DYNAMIC_RISKS_CHANGED",
    "probation-case.registration.added, RCPR, DYNAMIC_RISKS_CHANGED",
    "probation-case.registration.deleted, RCPR, DYNAMIC_RISKS_CHANGED",
    "probation-case.registration.deregistered, RCPR, DYNAMIC_RISKS_CHANGED",
    "probation-case.registration.updated, RCPR, DYNAMIC_RISKS_CHANGED",
    "probation-case.registration.added, RVAD, DYNAMIC_RISKS_CHANGED",
    "probation-case.registration.deleted, RVAD, DYNAMIC_RISKS_CHANGED",
    "probation-case.registration.deregistered, RVAD, DYNAMIC_RISKS_CHANGED",
    "probation-case.registration.updated, RVAD, DYNAMIC_RISKS_CHANGED",
    "probation-case.registration.added, STRG, DYNAMIC_RISKS_CHANGED",
    "probation-case.registration.deleted, STRG, DYNAMIC_RISKS_CHANGED",
    "probation-case.registration.deregistered, STRG, DYNAMIC_RISKS_CHANGED",
    "probation-case.registration.updated, STRG, DYNAMIC_RISKS_CHANGED",
    "probation-case.registration.added, AVIS, DYNAMIC_RISKS_CHANGED",
    "probation-case.registration.deleted, AVIS, DYNAMIC_RISKS_CHANGED",
    "probation-case.registration.deregistered, AVIS, DYNAMIC_RISKS_CHANGED",
    "probation-case.registration.updated, AVIS, DYNAMIC_RISKS_CHANGED",
    "probation-case.registration.added, WEAP, DYNAMIC_RISKS_CHANGED",
    "probation-case.registration.deleted, WEAP, DYNAMIC_RISKS_CHANGED",
    "probation-case.registration.deregistered, WEAP, DYNAMIC_RISKS_CHANGED",
    "probation-case.registration.updated, WEAP, DYNAMIC_RISKS_CHANGED",
    "probation-case.registration.added, RLRH, DYNAMIC_RISKS_CHANGED",
    "probation-case.registration.deleted, RLRH, DYNAMIC_RISKS_CHANGED",
    "probation-case.registration.deregistered, RLRH, DYNAMIC_RISKS_CHANGED",
    "probation-case.registration.updated, RLRH, DYNAMIC_RISKS_CHANGED",
    "probation-case.registration.added, RMRH, DYNAMIC_RISKS_CHANGED",
    "probation-case.registration.deleted, RMRH, DYNAMIC_RISKS_CHANGED",
    "probation-case.registration.deregistered, RMRH, DYNAMIC_RISKS_CHANGED",
    "probation-case.registration.updated, RMRH, DYNAMIC_RISKS_CHANGED",
    "probation-case.registration.added, RHRH, DYNAMIC_RISKS_CHANGED",
    "probation-case.registration.deleted, RHRH, DYNAMIC_RISKS_CHANGED",
    "probation-case.registration.deregistered, RHRH, DYNAMIC_RISKS_CHANGED",
    "probation-case.registration.updated, RHRH, DYNAMIC_RISKS_CHANGED",
  )
  fun `will process and save a person status event`(
    eventType: String,
    registerTypeCode: String,
    integrationEvent: String,
  ) {
    val hmppsEventRawMessage = sqsNotificationHelper.generateRawHmppsDomainEvent(eventType, registerTypeCode = registerTypeCode)
    assumeIdentities(hmppsId = crn)

    onDomainEventShouldCreateEventNotification(hmppsEventRawMessage, integrationEvent)
  }

  @Test
  fun `when a valid registration added sqs event is received, it should create event notification MAPPA_DETAIL_CHANGED`() {
    val hmppsEventRawMessage = sqsNotificationHelper.generateRawHmppsDomainEvent()
    val expectedNotificationType = IntegrationEventType.MAPPA_DETAIL_CHANGED.toString()
    assumeIdentities(hmppsId = crn)

    onDomainEventShouldCreateEventNotification(hmppsEventRawMessage, expectedNotificationType)
  }

  @Test
  fun `when a valid registration updated sqs event is received, it should create event notification MAPPA_DETAIL_CHANGED`() {
    val hmppsEventRawMessage = sqsNotificationHelper.generateRawHmppsDomainEvent("probation-case.registration.updated")
    val expectedNotificationType = IntegrationEventType.MAPPA_DETAIL_CHANGED.toString()
    assumeIdentities(hmppsId = crn)

    onDomainEventShouldCreateEventNotification(hmppsEventRawMessage, expectedNotificationType)
  }

  @Test
  fun `when an invalid SQS message (domain event) is received it should not create notification`() {
    val rawMessage = "Invalid JSON message"

    assertThrows<JsonParseException> { domainEventsListener.onDomainEvent(rawMessage) }

    verify { eventNotificationRepository wasNot Called }
  }

  @Test
  fun `when an unexpected event type is received, it should not create event notification`() {
    val unexpectedEventType = "unexpected.event.type"
    val hmppsEventRawMessage = sqsNotificationHelper.generateRawHmppsDomainEvent(eventType = unexpectedEventType)
    assumeIdentities(hmppsId = crn)

    onDomainEventShouldNotCreateEventNotification(hmppsEventRawMessage)
  }

  @Test
  fun `will not process and save a domain registration event message of none MAPP type`() =
    onDomainEventShouldNotCreateEventNotification(
      hmppsEventRawMessage = sqsNotificationHelper.generateRawHmppsDomainEvent(registerTypeCode = "NOTMAPP"),
    )

  @Test
  fun `when alert event matches multiple filters using generator, both services should be called`() {
    val hmppsEventRawMessage = sqsNotificationHelper.generateRawHmppsDomainEventWithAlertCode(eventType = "person.alert.created", alertCode = "HA")
    val expectedNotificationTypes =
      arrayOf(
        "PERSON_PND_ALERTS_CHANGED",
        "PERSON_ALERTS_CHANGED",
      )
    assumeIdentities(hmppsId = crn)

    onDomainEventShouldCreateEventNotifications(hmppsEventRawMessage, *expectedNotificationTypes)
  }

  @Nested
  inner class GivenErrorOfEventExecution {
    private val error = IllegalStateException("Something went wrong")
    private val rawMessage = sqsNotificationHelper.generateRawHmppsDomainEvent()
    private val hmppsDomainEvent = sqsNotificationHelper.createHmppsDomainEvent()
    private val wrappedErrorMessage = "Error executing HmppsDomainEvent"

    @AfterEach
    internal fun tearDown() {
      clearAllMocks()
    }

    @Test
    fun `when there is CompletionException, the error cause shall be extracted and logged`() =
      onDomainEventShouldThrowError(
        wrappedError = CompletionException(wrappedErrorMessage, error),
      )

    @Test
    fun `when there is AsyncAdapterBlockingExecutionFailedException, the error cause shall be extracted and logged`() =
      onDomainEventShouldThrowError(
        wrappedError = AsyncAdapterBlockingExecutionFailedException(wrappedErrorMessage, error),
      )

    @Test
    fun `when there is ListenerExecutionFailedException, the error cause shall be extracted and logged`() =
      onDomainEventShouldThrowError(
        wrappedError = ListenerExecutionFailedException(wrappedErrorMessage, error, GenericMessage(rawMessage)),
      )

    @Test
    fun `when there is CompletionException without cause, the error shall be logged without extracting message from cause `() {
      val errorWithoutCause = CompletionException("Something went wrong", null)
      onDomainEventShouldThrowError(
        wrappedError = errorWithoutCause,
        unwrappedError = errorWithoutCause,
      )
    }

    private inline fun <reified T : Throwable> onDomainEventShouldThrowError(
      wrappedError: T,
      unwrappedError: Throwable = error,
    ) {
      // Arrange
      every { domainEventIdentitiesResolver.getHmppsId(hmppsDomainEvent) } throws wrappedError

      // Act, Assert (error)
      assertThrows<T> { domainEventsListener.onDomainEvent(rawMessage) }

      // Assert (verify)
      verify(exactly = 1) { telemetryService.captureException(match { it.message == unwrappedError.message }) }
    }
  }

  @Test
  fun `when a valid SQS message (domain event) is received it should create notification`() {
    val rawMessage = sqsNotificationHelper.generateRawHmppsDomainEvent()
    val expectedEvent = IntegrationEventType.MAPPA_DETAIL_CHANGED.toString()
    assumeIdentities(hmppsId = crn)

    domainEventsListener.onDomainEvent(rawMessage)

    verify(exactly = 1) { eventNotificationRepository.insert(match { it.eventType == expectedEvent }) }
  }
}

abstract class DomainEventsListenerTestCase {
  companion object {
    @JvmStatic
    protected val baseUrl = "https://dev.integration-api.hmpps.service.justice.gov.uk"
  }

  protected val currentTime: LocalDateTime = LocalDateTime.now()
  protected val zonedCurrentTime: ZonedDateTime = currentTime.atZone(ZoneId.systemDefault())
  protected val testClock: Clock = Clock.fixed(zonedCurrentTime.toInstant(), zonedCurrentTime.zone)
  protected val sqsNotificationHelper by lazy {
    SqsNotificationGeneratingHelper(
      timestamp = zonedCurrentTime,
    )
  }

  protected val eventNotificationRepository = mockk<EventNotificationRepository>()
  protected val domainEventIdentitiesResolver = mockk<DomainEventIdentitiesResolver>()
  protected val telemetryService = mockk<TelemetryService>()
  protected val featureFlagTestConfig = FeatureFlagTestConfig()

  protected val deduplicationDomainEventService =
    DeduplicationDomainEventService(
      eventNotificationRepository,
      domainEventIdentitiesResolver,
      baseUrl,
      testClock,
      featureFlagTestConfig.featureFlagConfig,
    )

  val domainEventsListener = DomainEventsListener(deduplicationDomainEventService, telemetryService)

  @BeforeEach
  open fun setupEventTest() {
    // Enable all associated event types, for listener event testing
    val enabledFeatureFlags =
      listOf(
        FeatureFlagConfig.PERSON_LANGUAGES_CHANGED_NOTIFICATIONS_ENABLED,
        FeatureFlagConfig.PRISONER_BASE_LOCATION_CHANGED_NOTIFICATIONS_ENABLED,
        FeatureFlagConfig.PRISONER_MERGED_NOTIFICATIONS_ENABLED,
      )

    enabledFeatureFlags.forEach { featureFlagTestConfig.assumeFeatureFlag(it, true) }

    featureFlagTestConfig.assumeFeatureFlag(FeatureFlagConfig.DEDUPLICATE_EVENTS, true)

    every { eventNotificationRepository.insert(any()) } returnsArgument 0

    every { telemetryService.captureException(any()) } just Runs
  }

  @AfterEach
  fun cleanupEventTest() {
    clearAllMocks()
  }

  protected fun onDomainEventShouldCreateEventNotification(
    hmppsEventRawMessage: String,
    hmppsId: String,
    expectedNotificationType: String,
  ) {
    assumeIdentities(hmppsId = hmppsId)
    onDomainEventShouldCreateEventNotifications(hmppsEventRawMessage, expectedNotificationType)
  }

  protected fun onDomainEventShouldCreateEventNotification(
    hmppsEventRawMessage: String,
    expectedNotificationType: String,
  ) = onDomainEventShouldCreateEventNotifications(hmppsEventRawMessage, expectedNotificationType)

  protected fun onDomainEventShouldCreateEventNotifications(
    hmppsEventRawMessage: String,
    vararg expectedNotificationType: String,
  ) {
    // Act
    domainEventsListener.onDomainEvent(hmppsEventRawMessage)

    // Assert
    expectedNotificationType.forEach { expectedEventType ->
      verify(exactly = 1) { eventNotificationRepository.insert(match { it.eventType == expectedEventType }) }
    }
  }

  protected fun executeShouldSaveEventNotification(
    hmppsDomainEvent: HmppsDomainEvent,
    expectedEventNotification: EventNotification,
  ) = executeShouldSaveEventNotifications(
    hmppsDomainEvent = hmppsDomainEvent,
    expectedEventNotifications = listOf(expectedEventNotification),
  )

  protected fun executeShouldSaveEventNotifications(
    hmppsDomainEvent: HmppsDomainEvent,
    expectedEventNotifications: List<EventNotification>,
  ) {
    // Act
    deduplicationDomainEventService.execute(hmppsDomainEvent)

    // Assert
    expectedEventNotifications.forEach { expectedNotification ->
      // Verify all expected event notifications persisted via repository
      verify(exactly = 1) { eventNotificationRepository.insert(expectedNotification) }
    }
  }

  protected fun onDomainEventShouldNotCreateEventNotification(hmppsEventRawMessage: String) {
    // Act
    domainEventsListener.onDomainEvent(hmppsEventRawMessage)

    // Assert
    verify { eventNotificationRepository wasNot Called }
  }

  protected fun assumeIdentities(
    hmppsId: String? = null,
    prisonId: String? = null,
  ) {
    every { domainEventIdentitiesResolver.getHmppsId(any()) } returns hmppsId
    every { domainEventIdentitiesResolver.getPrisonId(any()) } returns prisonId
    every { domainEventIdentitiesResolver.getSupervisionStatus(any()) } returns "PRISONS"
  }
}
