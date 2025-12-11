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
    const val PRISON_OFFENDER_CACHE = "PRISON_OFFENDER_CACHE"
  }

  fun prisonerCache(): CaffeineCache =
    CaffeineCache(
      PRISON_OFFENDER_CACHE,
      Caffeine
        .newBuilder()
        .maximumSize(100)
        .expireAfterWrite(Duration.ofSeconds(2))
        .build(),
    )

  @Bean
  fun caffeineCacheManager(): CacheManager {
    val cacheManager = SimpleCacheManager()
    val caches = listOf(prisonerCache())
    cacheManager.setCaches(caches)
    return cacheManager
  }
}
