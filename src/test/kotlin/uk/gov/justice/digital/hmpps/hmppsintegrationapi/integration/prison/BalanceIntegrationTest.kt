package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.prison

import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase

class BalanceIntegrationTest : IntegrationTestBase() {
  private final val hmppsId = "G2996UX"
  private final val prisonId = "ABC"
  private final val basePrisonPath = "/v1/prison/$prisonId/prisoners/$hmppsId/balances"

  @Test
  fun `return a prisoner with all fields populated`() {
    callApi(basePrisonPath)
      .andExpect(status().isOk)
      .andExpect(content().json(""))
  }

//  @Test
//  fun `return a 404 for prisoner in wrong prison`() {
//    val headers = HttpHeaders()
//    headers.set("subject-distinguished-name", "C=GB,ST=London,L=London,O=Home Office,CN=limited-prisons")
//    mockMvc.perform(
//      get("$basePrisonPath/prisoners/$hmppsId").headers(headers),
//    )
//      .andExpect(status().isNotFound)
//  }
//
//  @Test
//  fun `return a 404 for if consumer has empty list of prisons`() {
//    val headers = HttpHeaders()
//    headers.set("subject-distinguished-name", "C=GB,ST=London,L=London,O=Home Office,CN=no-prisons")
//    mockMvc.perform(
//      get("$basePrisonPath/prisoners/$hmppsId").headers(headers),
//    )
//      .andExpect(status().isNotFound)
//  }
//
//  @Test
//  fun `return multiple prisoners when querying by complex parameters`() {
//    callApi("$basePrisonPath/")
//      .andExpect(status().isOk)
//      .andExpect(content().json(getExpectedResponse("prisoners-response")))
//  }
}
