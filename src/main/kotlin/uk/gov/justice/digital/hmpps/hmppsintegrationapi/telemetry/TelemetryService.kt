package uk.gov.justice.digital.hmpps.hmppsintegrationapi.telemetry

import com.microsoft.applicationinsights.TelemetryClient
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
class TelemetryService(
  private val telemetryClient: TelemetryClient = TelemetryClient(),
) {
  @Async
  fun trackEvent(name: String) {
    telemetryClient.trackEvent(name)
  }
}
