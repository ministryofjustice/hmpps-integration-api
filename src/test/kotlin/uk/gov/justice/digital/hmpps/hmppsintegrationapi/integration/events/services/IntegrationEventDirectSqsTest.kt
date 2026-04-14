package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.events.services

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.NullSource
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.bean.override.mockito.MockReset
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.EventNotification
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.Filters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.IntegrationEventStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.enums.IntegrationEventType
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.repository.JdbcTemplateEventNotificationRepository
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.services.EventNotificationService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.services.SendEventsService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestInMemoryQueueBase
import java.time.LocalDateTime
import java.util.UUID
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit

class IntegrationEventDirectSqsTest : IntegrationTestInMemoryQueueBase("testqueue") {
  @Autowired
  private lateinit var stateEventNotifierService: SendEventsService

  @Autowired
  private lateinit var eventRepository: JdbcTemplateEventNotificationRepository

  @MockitoSpyBean(reset = MockReset.BEFORE)
  private lateinit var eventNotificationService: EventNotificationService

  @BeforeEach
  fun purgeQueues() {
    Mockito.reset(eventNotificationService)
    whenever(featureFlagConfig.isEnabled(FeatureFlagConfig.DIRECT_SQS_NOTIFICATIONS)).thenReturn(true)
    testQueue.purge()
    eventRepository.deleteAll()
  }

  fun getEvent(
    prisonId: String? = null,
    url: String,
    filters: Filters? = null,
    eventType: String = IntegrationEventType.MAPPA_DETAIL_CHANGED.name,
  ) = EventNotification(
    status = IntegrationEventStatus.PENDING.name,
    eventType = eventType,
    hmppsId = "MockId",
    prisonId = prisonId,
    url = url,
    lastModifiedDatetime = LocalDateTime.now().minusMinutes(6),
    filters = filters,
  )

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = ["MKI"])
  @DisplayName("will publish Integration Event direct to consumer queue")
  @Throws(
    ExecutionException::class,
    InterruptedException::class,
  )
  fun willPublishDirectToQueue(prisonId: String?) {
    val event = getEvent(prisonId, UUID.randomUUID().toString())
    eventRepository.save(event)
    stateEventNotifierService.sentNotifications()

    await.atMost(5, TimeUnit.SECONDS).untilAsserted {
      Mockito.verify(eventNotificationService, Mockito.atLeast(1)).sendEvent(any())
      val prisonEventMessages = testQueueService.getQueue("testqueue").messagesAsObjects<EventNotification>()
      assertThat(prisonEventMessages).hasSize(1)
      assertThat(prisonEventMessages[0].eventType).isEqualTo(event.eventType)
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("events")
  fun directSendWithFiltersTest(
    display: String,
    consumerSupervisionStatuses: List<String>?,
    messageSupervisionStatus: Filters?,
    consumerPrisons: List<String>?,
    messagePrisonId: String?,
    shouldBeSent: Boolean,
  ) {
    val consumerConfig = authorisationConfig.consumers["automated-test-client"]
    val consumerFilters = authorisationConfig.allFilters("automated-test-client")?.copy(prisons = consumerPrisons, supervisionStatuses = consumerSupervisionStatuses)
    whenever(authorisationConfig.consumers).thenReturn(mapOf("automated-test-client" to consumerConfig?.copy(roles = listOf("mappa-cat4"), filters = consumerFilters)))

    val event = getEvent(messagePrisonId, UUID.randomUUID().toString(), messageSupervisionStatus)
    eventRepository.save(event)
    stateEventNotifierService.sentNotifications()

    await.atMost(5, TimeUnit.SECONDS).untilAsserted {
      Mockito.verify(eventNotificationService, Mockito.atLeast(1)).sendEvent(any())
      val eventMessages = testQueueService.getQueue("testqueue").messagesAsObjects<EventNotification>()
      if (shouldBeSent) {
        assertThat(eventMessages).hasSize(1)
        assertThat(eventMessages[0].eventType).isEqualTo(event.eventType)
      } else {
        assertThat(eventMessages).isEmpty()
      }
    }
  }

  companion object {
    @JvmStatic
    private fun events() =
      listOf(
        Arguments.of("Consumer has a supervision status of prisons, but the message does not contain a supervision status - Will not send event", listOf("PRISONS"), null, null, null, false),
        Arguments.of("Consumer has a supervision status of prisons, but the message contains a probation supervision status - Will not send event", listOf("PROBATION"), Filters(supervisionStatus = "PRISON"), null, null, false),
        Arguments.of("Consumer has no supervision status and the message contains a probation supervision status - Will still send event", null, Filters(supervisionStatus = "PRISON"), null, null, true),
        Arguments.of("Consumer has a prison filter with MKI, but the message does not contain a prisonId - will not send event", null, null, listOf("MKI"), null, false),
        Arguments.of("Consumer has a prison filter with MKI, but the message contains MDI - will not send event", null, null, listOf("MKI"), "MDI", false),
        Arguments.of("Consumer has a prison filter with MKI, the message contains MKI - will send event", null, null, listOf("MKI"), "MKI", true),
        Arguments.of("No prison or supervision status filter - Will still send event", null, null, null, null, true),
      )
  }
}
