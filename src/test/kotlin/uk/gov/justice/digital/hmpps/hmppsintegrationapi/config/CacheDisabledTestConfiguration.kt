package uk.gov.justice.digital.hmpps.hmppsintegrationapi.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.cache.CacheManager
import org.springframework.cache.support.NoOpCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

@TestConfiguration
class CacheDisabledTestConfiguration {
  @ConditionalOnProperty("cache-enabled", havingValue = "false")
  @Primary
  @Bean("noOpCacheManager")
  fun caffeineCacheManager(): CacheManager = NoOpCacheManager()
}
