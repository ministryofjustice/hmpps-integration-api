package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class TransactionsIntegrationTest : IntegrationTestBase() {
  val prisonId = "MDI"
  val hmppsId = "G2996UX"
  val accountCode = "spends"
  final val fromDate = "2024-01-01"
  final val toDate = "2024-01-14"
  val transactionsPath = "/v1/prison"
  var dateQueryParams = "?from_date=$fromDate&to_date=$toDate"

  @Test
  fun `return a list of transactions for a prisoner`() {
    callApi("/v1/prison/$prisonId/prisoners/$hmppsId/transactions/$accountCode")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("transactions-response")))
  }

  @Test
  fun `return a list of transactions when the dates are supplied in the request`() {
    val headers = org.springframework.http.HttpHeaders()
    headers.set("subject-distinguished-name", "C=GB,ST=London,L=London,O=Home Office,CN=automated-test-client")
    mockMvc
      .perform(
        get("$transactionsPath/$prisonId/prisoners/$hmppsId/transactions/$accountCode$dateQueryParams").headers(headers),
      ).andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("transactions-response")))
  }

  @Test
  fun `returns no results`() {
    var wrongPrisonId = "XYZ"
    val headers = org.springframework.http.HttpHeaders()
    headers.set("subject-distinguished-name", "C=GB,ST=London,L=London,O=Home Office,CN=limited-prisons")
    mockMvc
      .perform(
        get("$transactionsPath/$wrongPrisonId/prisoners/$hmppsId/transactions/$accountCode").headers(headers),
      ).andExpect(status().isNotFound)
  }
}
