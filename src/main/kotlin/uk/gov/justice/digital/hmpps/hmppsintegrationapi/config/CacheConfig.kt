package uk.gov.justice.digital.hmpps.hmppsintegrationapi.config

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCache
import org.springframework.cache.support.SimpleCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@EnableCaching
@Configuration
class CacheConfig {
  companion object {
    const val GATEWAY_CACHE = "GATEWAY_CACHE"
  }

  fun gatewayCache(): CaffeineCache =
    CaffeineCache(
      GATEWAY_CACHE,
      Caffeine
        .newBuilder()
        .maximumSize(100)
        .expireAfterWrite(Duration.ofSeconds(2))
        .build(),
    )

  @Bean
  fun caffeineCacheManager(): CacheManager {
    val cacheManager = SimpleCacheManager()
    val caches = listOf(gatewayCache())
    cacheManager.setCaches(caches)
    return cacheManager
  }
}
