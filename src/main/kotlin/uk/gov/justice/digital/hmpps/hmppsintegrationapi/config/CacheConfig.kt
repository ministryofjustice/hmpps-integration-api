package uk.gov.justice.digital.hmpps.hmppsintegrationapi.config

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCache
import org.springframework.cache.interceptor.KeyGenerator
import org.springframework.cache.support.NoOpCacheManager
import org.springframework.cache.support.SimpleCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.util.StringUtils
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.GATEWAY_CACHE_ENABLED
import java.lang.reflect.Method
import java.time.Duration

@EnableCaching
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
