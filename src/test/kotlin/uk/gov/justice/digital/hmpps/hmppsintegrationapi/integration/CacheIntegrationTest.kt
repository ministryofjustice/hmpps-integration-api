package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyMap
import org.mockito.kotlin.atLeast
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.caffeine.CaffeineCache
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.GATEWAY_CACHE_METRICS
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.telemetry.TelemetryService
import kotlin.test.assertEquals

@TestPropertySource(properties = ["feature-flag.gateway-cache-enabled=true"])
class CacheIntegrationTest : IntegrationTestBase() {
  private final val hmppsId = "G2996UX"
  private final val path = "/v1/persons/$hmppsId/addresses"

  @Autowired
  lateinit var cache: CaffeineCache

  @MockitoBean
  lateinit var telemetryService: TelemetryService

  @MockitoSpyBean
  private lateinit var prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway

  @Test
  fun `caches prisoner data when addresses endpoint called twice and feature enabled`() {
    // Request 1
    callApiWithCN(path, specificPrisonCn)
      .andExpect(status().isOk)

    // Reqyest 2
    callApiWithCN(path, specificPrisonCn)
      .andExpect(status().isOk)

    // Calls the cached method only once
    verify(prisonerOffenderSearchGateway, times(1)).getPrisonOffender(hmppsId)

    // 1 cache miss for the first request
    assertEquals(cache.nativeCache.stats().missCount(), 1L)

    // 1 cache hit for the second request
    assertEquals(cache.nativeCache.stats().hitCount(), 1L)

    verify(telemetryService, atLeast(1)).trackEvent(eq(GATEWAY_CACHE_METRICS), anyMap(), anyMap())
  }
}

@TestPropertySource(properties = ["feature-flag.gateway-cache-enabled=false"])
class CacheDisabledIntegrationTest : IntegrationTestBase() {
  private final val hmppsId = "G2996UX"
  private final val path = "/v1/persons/$hmppsId/addresses"

  @MockitoBean
  lateinit var telemetryService: TelemetryService

  @MockitoSpyBean
  private lateinit var prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway

  @Test
  fun `does not cache prisoner data when addresses endpoint called twice and feature disabled`() {
    // Request 1
    callApiWithCN(path, specificPrisonCn)
      .andExpect(status().isOk)

    // Reqyest 2
    callApiWithCN(path, specificPrisonCn)
      .andExpect(status().isOk)

    // Calls the cacheable method twice (does not cache)
    verify(prisonerOffenderSearchGateway, times(2)).getPrisonOffender(hmppsId)

    verify(telemetryService, never()).trackEvent(eq(GATEWAY_CACHE_METRICS), anyMap(), anyMap())
  }
}
