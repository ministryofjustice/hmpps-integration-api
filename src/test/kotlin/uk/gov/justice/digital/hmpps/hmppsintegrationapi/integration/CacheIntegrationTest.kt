package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.caffeine.CaffeineCache
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import kotlin.test.assertEquals

class CacheIntegrationTest : IntegrationTestBase() {
  private final val nomsPath = "/v1/persons/$nomsId"
  private final val crnPath = "/v1/persons/$crn"
  private final val addressPath = "$nomsPath/addresses"

  @Autowired
  lateinit var cache: CaffeineCache

  @Test
  fun `caches prisoner and cpr data when addresses endpoint called twice and feature enabled`() {
    // Request 1
    callApiWithCN(addressPath, specificPrisonCn)
      .andExpect(status().isOk)

    // Reqyest 2
    callApiWithCN(addressPath, specificPrisonCn)
      .andExpect(status().isOk)

    // Calls the cached method only once
    verify(prisonerOffenderSearchGateway, times(1)).getPrisonOffender(nomsId)

    // Calls the cached CPR method only once
    verify(corePersonRecordGateway, times(1)).corePersonRecordFor(any(), eq(nomsId))

    // Address endpoint calls CPR twice per request. One for nomis and one for crn
    // 2 cache misses for the first request
    assertEquals(2L, cache.nativeCache.stats().missCount())

    // 4 cache hits after the second request
    assertEquals(4L, cache.nativeCache.stats().hitCount())
  }

  @Test
  fun `does caches offender data when crn endpoint called twice and feature enabled`() {
    // Request 1
    callApiWithCN(crnPath, specificPrisonCn)
      .andExpect(status().isOk)

    // Request 2
    callApiWithCN(crnPath, specificPrisonCn)
      .andExpect(status().isOk)

    // Calls the cacheable method only once (caches first request)
    verify(nDeliusGateway, times(1)).getOffender(crn)
  }
}

@TestPropertySource(properties = ["feature-flag.gateway-cache-enabled=false"])
class CacheDisabledIntegrationTest : IntegrationTestBase() {
  private final val nomsPath = "/v1/persons/$nomsId"
  private final val crnPath = "/v1/persons/$crn"
  private final val addressPath = "$nomsPath/addresses"

  @Test
  fun `does not cache prisoner data when addresses endpoint called twice and feature disabled`() {
    // Request 1
    callApiWithCN(addressPath, specificPrisonCn)
      .andExpect(status().isOk)

    // Request 2
    callApiWithCN(addressPath, specificPrisonCn)
      .andExpect(status().isOk)

    // Calls the cacheable method twice (does not cache)
    verify(prisonerOffenderSearchGateway, times(2)).getPrisonOffender(nomsId)

    // Address endpoint calls CPR twice per request. One for nomis and one for crn
    // Calls the cached CPR method 4 times in total across 2 requests
    verify(corePersonRecordGateway, times(4)).corePersonRecordFor(any(), eq(nomsId))
  }

  @Test
  fun `does not cache offender data when crn endpoint called twice and feature disabled`() {
    // Request 1
    callApiWithCN(crnPath, specificPrisonCn)
      .andExpect(status().isOk)

    // Request 2
    callApiWithCN(crnPath, specificPrisonCn)
      .andExpect(status().isOk)
    // Calls the cacheable method twice (does not cache)
    verify(nDeliusGateway, times(2)).getOffender(crn)
  }
}
