package uk.gov.justice.digital.hmpps.hmppsintegrationapi.telemetry

import com.microsoft.applicationinsights.TelemetryClient
import io.sentry.Sentry
import org.jetbrains.annotations.NotNull
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
class TelemetryService(
  private val telemetryClient: TelemetryClient = TelemetryClient(),
) {
  @Async
  fun trackEvent(
    name: String,
    properties: Map<String, String?> = mapOf(),
    metrics: Map<String, Double?> = mapOf(),
  ) {
    telemetryClient.trackEvent(
      name,
      properties.filterValues { it != null },
      metrics.filterValues { it != null },
    )
  }

  fun captureException(
    @NotNull throwable: Throwable,
  ) {
    Sentry.captureException(throwable)
  }
}
