package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.events.service

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertDoesNotThrow
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.repository.EventNotificationRepository
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.services.DeleteProcessedEventsService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.telemetry.TelemetryService
import kotlin.test.Test

class DeleteProcessedEventsServiceTest {
  val repo: EventNotificationRepository = mock(EventNotificationRepository::class.java)
  val telemetry: TelemetryService = mock(TelemetryService::class.java)

  @BeforeEach
  fun setup() {
    reset(repo, telemetry)
  }

  @Test
  fun `completes successfully`() {
    assertDoesNotThrow { DeleteProcessedEventsService(repo, telemetry).deleteProcessedEvents() }
  }

  @Test
  fun `logs an error to sentry`() {
    val ex = RuntimeException("Something went wrong")
    whenever(repo.deleteEvents(any())).thenThrow(ex)
    DeleteProcessedEventsService(repo, telemetry).deleteProcessedEvents()
    verify(telemetry).captureException(ex)
  }
}
