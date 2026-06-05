package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@TestPropertySource(properties = ["cache-enabled=true"])
class CacheIntegrationTest : IntegrationTestBase() {
  private final val nomsPath = "/v1/persons/$nomsId"
  private final val crnPath = "/v1/persons/$crn"
  private final val addressPath = "$nomsPath/addresses"

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
