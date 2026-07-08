package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.onbehalfof.createUnsignedJwt

@TestPropertySource(properties = ["cache-enabled=true"])
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class CacheIntegrationTest : IntegrationTestBase() {
  private final val nomsPath = "/v1/persons/$nomsId"
  private final val crnPath = "/v1/persons/$crn"
  private final val addressPath = "$nomsPath/addresses"

  @Order(1)
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

  @Order(2)
  @Test
  fun `does caches offender data when crn endpoint called twice and feature enabled`() {
    // Request 1
    callApiWithCN(crnPath, specificPrisonCn)
      .andExpect(status().isOk)

    // Request 2
    callApiWithCN(crnPath, specificPrisonCn)
      .andExpect(status().isOk)

    // Calls the cacheable method only once (caches first request)
    verify(nDeliusGateway, times(1)).getOffender(eq(crn), any<RequestContext>())
  }

  @Order(3)
  @Test
  fun `caches manage users data when crn endpoint called twice and feature enabled with obo`() {
    // Request 1
    callApiWithCN(crnPath, "obo-unsigned-verified", oboValue = createUnsignedJwt())
      .andExpect(status().isOk)

    // Request 2
    callApiWithCN(crnPath, "obo-unsigned-verified", oboValue = createUnsignedJwt())
      .andExpect(status().isOk)

    // Calls the cacheable method only once (caches first request)
    verify(manageUsersGateway, times(1)).findUser(any(), any())
  }
}

class CacheDisabledIntegrationTest : IntegrationTestBase() {
  private final val nomsPath = "/v1/persons/$nomsId"
  private final val crnPath = "/v1/persons/$crn"
  private final val addressPath = "$nomsPath/addresses"

  private final val oboCn = "obo-unsigned-verified"

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
    verify(nDeliusGateway, times(2)).getOffender(eq(crn), any<RequestContext>())
  }

  @Test
  fun `does not cache find user data from manangeUsersGateway when addresses endpoint called twice and feature disabled with obo`() {
    // Request 1UnsignedJwtOboService
    callApiWithCN(addressPath, oboCn, oboValue = createUnsignedJwt())
      .andExpect(status().isOk)

    // Reqyest 2
    callApiWithCN(addressPath, oboCn, oboValue = createUnsignedJwt())
      .andExpect(status().isOk)

    // Calls the cached manage users only once
    verify(manageUsersGateway, times(2)).findUser(any(), any())
  }
}
