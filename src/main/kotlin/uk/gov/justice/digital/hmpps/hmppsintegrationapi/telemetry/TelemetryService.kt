package uk.gov.justice.digital.hmpps.hmppsintegrationapi.telemetry

import com.microsoft.applicationinsights.TelemetryClient
import io.opentelemetry.api.trace.Span
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

  fun captureMessage(message: String) {
    Sentry.captureMessage(message)
  }

  fun setSpanAttribute(
    key: String,
    value: String,
  ) {
    try {
      Span.current().setAttribute(key, value)
    } catch (ignored: Exception) {
      // Do nothing - don't create span
    }
  }
}
