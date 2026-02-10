package uk.gov.justice.digital.hmpps.hmppsintegrationapi.config

import com.github.benmanes.caffeine.cache.Caffeine
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyMap
import org.mockito.Mockito.mock
import org.mockito.kotlin.atLeast
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.cache.caffeine.CaffeineCache
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.telemetry.TelemetryService

class CacheMetricsListenerTest : ConfigTest() {
  companion object {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)
  }

  @Test
  fun `logs metrics to app insights`() {
    val gatewayCache = mock(CaffeineCache::class.java)
    val telemetryService = mock(TelemetryService::class.java)
    val cache = Caffeine.newBuilder().build<Any, Any>()
    whenever(gatewayCache.nativeCache).thenReturn(cache)
    val metricsListener = CacheMetricsListener(gatewayCache, telemetryService)
    metricsListener.logCacheMetrics()
    verify(telemetryService, atLeast(1)).trackEvent(eq(GATEWAY_CACHE_METRICS), anyMap(), anyMap())
  }
}
