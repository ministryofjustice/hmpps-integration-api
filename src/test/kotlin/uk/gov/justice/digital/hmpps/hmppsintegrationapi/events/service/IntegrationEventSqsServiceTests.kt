package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.service

import io.kotest.matchers.string.shouldContain
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.AuthorisationConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.ConfigTest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.EventNotification
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.Metadata
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.services.EventNotificationService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.MockMvcExtensions.objectMapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.TestQueueService
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import java.time.LocalDateTime

class IntegrationEventSqsServiceTests : ConfigTest() {
  val topicService: HmppsQueueService = mock()
  private lateinit var queueService: TestQueueService
  val authorisationConfig: AuthorisationConfig = mock()
  val featureFlagConfig: FeatureFlagConfig = FeatureFlagConfig()
  val telemetryService: TelemetryService = mock()

  private lateinit var eventNotificationService: EventNotificationService
  val currentTime: LocalDateTime = LocalDateTime.now()

  val event =
    EventNotification(
      eventId = 123,
      hmppsId = "hmppsId",
      eventType = "MAPPA_DETAIL_CHANGED",
      prisonId = "MKI",
      url = "mockUrl",
      lastModifiedDatetime = currentTime,
      claimId = null,
    )

  @BeforeEach
  fun setUp() {
    queueService = TestQueueService(queues = listOf("mockQueue1, mockQueue2"))
    whenever(authorisationConfig.consumersWithQueue()).thenReturn(setOf("mockConsumer1", "mockConsumer2"))
    whenever(authorisationConfig.events(any())).thenReturn(listOf(event.eventType))
    whenever(authorisationConfig.consumers).thenReturn(
      mapOf(
        "mockConsumer1" to ConsumerConfig(queueName = "mockQueue1"),
        "mockConsumer2" to ConsumerConfig(queueName = "mockQueue2"),
      ),
    )
  }

  @Test
  fun `Publish Event to multiple queues is successful`() {
    // Setup both queues
    queueService = TestQueueService(queues = listOf("mockQueue1", "mockQueue2"))
    eventNotificationService = EventNotificationService(topicService, queueService, objectMapper, authorisationConfig, featureFlagConfig, telemetryService)
    eventNotificationService.sendEvent(event)
    val queue1Messages = queueService.getQueue("mockQueue1").messagesAsObjects<EventNotification>()
    val queue2Messages = queueService.getQueue("mockQueue2").messagesAsObjects<EventNotification>()

    // Check queue
    assertThat(queue1Messages[0]).isEqualTo(event)
    assertThat(queue2Messages[0]).isEqualTo(event)
  }

  @Test
  fun `Publish Event to first queue is unsuccessful, but second queue is successful`() {
    // Only setup the test queue to have mockQueue2
    queueService = TestQueueService(queues = listOf("mockQueue2"))
    eventNotificationService = EventNotificationService(topicService, queueService, objectMapper, authorisationConfig, featureFlagConfig, telemetryService)
    eventNotificationService.sendEvent(event)

    val queue2Messages = queueService.getQueue("mockQueue2").messagesAsObjects<EventNotification>()
    assertThat(queue2Messages[0]).isEqualTo(event)

    // Check that an exception is thrown
    argumentCaptor<Throwable>().apply {
      verify(telemetryService, times(1)).captureException(capture())
      firstValue.message.shouldContain("Queue mockQueue1 not found")
    }
  }

  @Test
  fun `event is applicable to the consumer`() {
    val testEvent = EventNotification(url = "url", eventType = "PERSON_STATUS_CHANGED")
    val config =
      parseConfig<AuthorisationConfig>(
        """
        consumers:
          tester:
            roles:
              - full-access
        """.trimIndent(),
      )

    eventNotificationService = EventNotificationService(topicService, queueService, objectMapper, config, featureFlagConfig, telemetryService)
    Assertions.assertTrue(eventNotificationService.isEventApplicable("tester", testEvent))
  }

  @Test
  fun `event is NOT applicable to the consumer based on event type`() {
    val testEvent = EventNotification(url = "url", eventType = "UNKNOWN_TYPE")
    val config =
      parseConfig<AuthorisationConfig>(
        """
        consumers:
          tester:
            roles:
              - full-access
        """.trimIndent(),
      )
    eventNotificationService = EventNotificationService(topicService, queueService, objectMapper, config, featureFlagConfig, telemetryService)
    Assertions.assertFalse(eventNotificationService.isEventApplicable("tester", testEvent))
  }

  @Test
  fun `event is applicable to the consumer based on the prison id`() {
    val testEvent = EventNotification(url = "url", eventType = "PERSON_ADDRESS_CHANGED", prisonId = "MKI")
    val config =
      parseConfig<AuthorisationConfig>(
        """
        consumers:
          tester:
            roles:
              - private-prison
            filters:
              prisons:
               - MKI
        """.trimIndent(),
      )
    eventNotificationService = EventNotificationService(topicService, queueService, objectMapper, config, featureFlagConfig, telemetryService)
    Assertions.assertTrue(eventNotificationService.isEventApplicable("tester", testEvent))
  }

  @Test
  fun `event is NOT applicable to the consumer based on the prison id`() {
    val testEvent = EventNotification(url = "url", eventType = "PERSON_ADDRESS_CHANGED", prisonId = "MDI")
    val config =
      parseConfig<AuthorisationConfig>(
        """
        consumers:
          tester:
            roles:
              - private-prison
            filters:
              prisons:
                - MKI
        """.trimIndent(),
      )
    eventNotificationService = EventNotificationService(topicService, queueService, objectMapper, config, featureFlagConfig, telemetryService)
    Assertions.assertFalse(eventNotificationService.isEventApplicable("tester", testEvent))
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("events")
  fun isEventApplicableTest(
    display: String,
    config: String,
    messagePrisonId: String?,
    messageSupervisionStatus: String?,
    shouldBeSent: Boolean,
  ) {
    val testEvent =
      EventNotification(
        url = "url",
        eventType = "PERSON_ADDRESS_CHANGED",
        prisonId = messagePrisonId,
        metadata = messageSupervisionStatus?.let { Metadata(it) },
      )
    val config = parseConfig<AuthorisationConfig>(config)
    eventNotificationService = EventNotificationService(topicService, queueService, objectMapper, config, featureFlagConfig, telemetryService)

    assertThat(eventNotificationService.isEventApplicable("tester", testEvent)).isEqualTo(shouldBeSent)
  }

  companion object {
    @JvmStatic
    private fun events() =
      listOf(
        Arguments.of(
          "Consumer has a supervision status of prisons, but the message does not contain a supervision status - Will not send event",
          """
          consumers:
            tester:
              roles:
                - private-prison
              filters:
                supervisionStatuses:
                  - PRISONS
          """.trimIndent(),
          null,
          null,
          false,
        ),
        Arguments.of(
          "Consumer has a supervision status of prisons, but the message contains a probation supervision status - Will not send event",
          """
          consumers:
            tester:
              roles:
                - private-prison
              filters:
                supervisionStatuses:
                  - PRISONS
          """.trimIndent(),
          null,
          "PROBATION",
          false,
        ),
        Arguments.of(
          "Consumer has a supervision status of prisons, and the message contains a prison supervision status - Will send the event",
          """
          consumers:
            tester:
              roles:
                - private-prison
              filters:
                supervisionStatuses:
                  - PRISONS
          """.trimIndent(),
          "MKI",
          "PRISONS",
          true,
        ),
        Arguments.of(
          "Consumer has no supervision status and the message contains a probation supervision status - Will still send event",
          """
          consumers:
            tester:
              roles:
                - private-prison
          """.trimIndent(),
          null,
          "PROBATION",
          true,
        ),
        Arguments.of(
          "Consumer has a prison filter with MKI, but the message does not contain a prisonId - will not send event",
          """
          consumers:
            tester:
              roles:
                - private-prison
              filters:
                prisons:
                  - MKI
          """.trimIndent(),
          null,
          null,
          false,
        ),
        // Should be set to false when supervision check in place
        Arguments.of(
          "Consumer has a prison filter with MKI, but the message contains MDI - will not send event",
          """
          consumers:
            tester:
              roles:
                - private-prison
              filters:
                prisons:
                  - MKI
          """.trimIndent(),
          "MDI",
          null,
          false,
        ),
        Arguments.of(
          "Consumer has a prison filter with MKI, the message contains MKI - will send event",
          """
          consumers:
            tester:
              roles:
                - private-prison
              filters:
                prisons:
                  - MKI
          """.trimIndent(),
          "MKI",
          null,
          true,
        ),
        Arguments.of(
          "No prison or supervision status filter - Will still send event",
          """
          consumers:
            tester:
              roles:
                - private-prison
          """.trimIndent(),
          "MKI",
          null,
          true,
        ),
      )
  }
}
