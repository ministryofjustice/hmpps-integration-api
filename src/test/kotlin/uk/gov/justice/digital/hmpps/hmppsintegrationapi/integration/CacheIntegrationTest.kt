package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import org.junit.jupiter.api.Test
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.caffeine.CaffeineCache
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import kotlin.test.assertEquals

class CacheIntegrationTest : IntegrationTestBase() {
  private final val hmppsId = "G2996UX"
  private final val path = "/v1/persons/$hmppsId/addresses"

  @Autowired
  lateinit var cache: CaffeineCache

  @MockitoSpyBean
  private lateinit var prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway

  @Test
  fun `caches prisoner data when addresses endpoint called twice`() {
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
  }
}
