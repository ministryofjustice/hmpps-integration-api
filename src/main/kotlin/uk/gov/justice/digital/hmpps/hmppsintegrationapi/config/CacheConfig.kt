package uk.gov.justice.digital.hmpps.hmppsintegrationapi.config

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.filter.Filter
import ch.qos.logback.core.spi.FilterReply
import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCache
import org.springframework.cache.interceptor.KeyGenerator
import org.springframework.cache.support.NoOpCacheManager
import org.springframework.cache.support.SimpleCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.GATEWAY_CACHE_ENABLED
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.telemetry.TelemetryService
import java.lang.reflect.Method
import java.time.Duration

@EnableCaching
@EnableScheduling
@Configuration
class CacheConfig {
  companion object {
    const val GATEWAY_CACHE = "GATEWAY_CACHE"
  }

  @Bean
  fun gatewayCache(): CaffeineCache =
    CaffeineCache(
      GATEWAY_CACHE,
      Caffeine
        .newBuilder()
        .maximumSize(100)
        .expireAfterWrite(Duration.ofSeconds(2))
        .recordStats()
        .build(),
    )

  @Bean
  fun gatewayCacheEnabled(featureFlagConfig: FeatureFlagConfig): Boolean = featureFlagConfig.isEnabled(GATEWAY_CACHE_ENABLED)

  @Bean
  fun caffeineCacheManager(gatewayCacheEnabled: Boolean): CacheManager {
    if (!gatewayCacheEnabled) {
      return NoOpCacheManager()
    }
    val cacheManager = SimpleCacheManager()
    val caches = listOf(gatewayCache())
    cacheManager.setCaches(caches)
    return cacheManager
  }

  @Bean("gatewayKeyGenerator")
  fun keyGenerator(): KeyGenerator = GatewayKeyGenerator()
}

/**
 * Generates a unique key for the cache so this can be used on all gateway methods
 */
class GatewayKeyGenerator : KeyGenerator {
  override fun generate(
    target: Any,
    method: Method,
    vararg params: Any?,
  ): Any = target.javaClass.name + "_" + method.name + "_" + StringUtils.arrayToDelimitedString(params, "_")
}

/**
 * Suppresses Log messages when the cache condition has failed
 */
class CacheLogFilter : Filter<ILoggingEvent>() {
  override fun decide(event: ILoggingEvent): FilterReply {
    if (event.message.contains("Cache condition failed")) {
      return FilterReply.DENY
    }
    return FilterReply.ACCEPT
  }
}

const val GATEWAY_CACHE_METRICS = "GatewayCacheMetrics"

@Component
@ConditionalOnProperty("feature-flag.gateway-cache-enabled", havingValue = "true")
@ConditionalOnBean(CaffeineCache::class)
class CacheMetricsListener(
  private val gatewayCache: CaffeineCache,
  private val telemetryService: TelemetryService,
) {
  @Scheduled(fixedDelay = 60000)
  fun logCacheMetrics() {
    val metrics = gatewayCache.nativeCache.stats()
    val hitRate = metrics.hitRate() * 100
    telemetryService.trackEvent(
      GATEWAY_CACHE_METRICS,
      mapOf(
        "hitRate" to hitRate.toString(),
        "hitCount" to metrics.hitCount().toString(),
        "missCount" to metrics.missCount().toString(),
        "loadSuccessCount" to metrics.loadSuccessCount().toString(),
        "loadFailureCount" to metrics.loadFailureCount().toString(),
        "totalLoadTime" to metrics.totalLoadTime().toString(),
        "evictionCount" to metrics.evictionCount().toString(),
        "evictionWeight" to metrics.evictionWeight().toString(),
      ),
    )
  }
}
