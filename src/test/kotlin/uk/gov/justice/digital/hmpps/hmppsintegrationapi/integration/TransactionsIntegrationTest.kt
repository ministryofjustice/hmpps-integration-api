package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class TransactionsIntegrationTest : IntegrationTestBase() {
  val prisonId = "MDI"
  val hmppsId = "G2996UX"
  val accountCode = "spends"
  final val fromDate = "2024-01-01"
  final val toDate = "2024-01-14"
  val clientUniqueRef = "ABC123456X"
  var dateQueryParams = "?from_date=$fromDate&to_date=$toDate"

  @Test
  fun `return a list of transactions for a prisoner`() {
    callApi("/v1/prison/$prisonId/prisoners/$hmppsId/accounts/$accountCode/transactions")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("transactions-response")))
  }

  @Test
  fun `return a list of transactions when the dates are supplied in the request`() {
    val headers = org.springframework.http.HttpHeaders()
    headers.set("subject-distinguished-name", "C=GB,ST=London,L=London,O=Home Office,CN=automated-test-client")
    mockMvc
      .perform(
        get("/v1/prison/$prisonId/prisoners/$hmppsId/accounts/$accountCode/transactions$dateQueryParams").headers(headers),
      ).andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("transactions-response")))
  }

  @Test
  fun `transactions returns no results`() {
    var wrongPrisonId = "XYZ"
    val headers = org.springframework.http.HttpHeaders()
    headers.set("subject-distinguished-name", "C=GB,ST=London,L=London,O=Home Office,CN=limited-prisons")
    mockMvc
      .perform(
        get("/v1/prison/$wrongPrisonId/prisoners/$hmppsId/accounts/$accountCode/transactions").headers(headers),
      ).andExpect(status().isNotFound)
  }

  // transaction
  @Test
  fun `return a transaction for a prisoner`() {
    callApi("/v1/prison/$prisonId/prisoners/$hmppsId/transactions/$clientUniqueRef")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("transaction-response")))
  }

  @Test
  fun `transaction returns no result`() {
    var wrongPrisonId = "XYZ"
    val headers = org.springframework.http.HttpHeaders()
    headers.set("subject-distinguished-name", "C=GB,ST=London,L=London,O=Home Office,CN=limited-prisons")
    mockMvc
      .perform(
        get("/v1/prison/$wrongPrisonId/prisoners/$hmppsId/transactions/$clientUniqueRef").headers(headers),
      ).andExpect(status().isNotFound)
  }

  // POST transaction
  @Test
  fun `return an expected response for a successful transaction post`() {
    val requestBody =
      """
      {
        "type": "CANT",
        "description": "Canteen Purchase of £16.34",
        "amount": 1634,
        "clientTransactionId": "CL123212",
        "clientUniqueRef": "CLIENT121131-0_11"
      }
      """.trimIndent()

    val headers = org.springframework.http.HttpHeaders()
    headers.set("subject-distinguished-name", "C=GB,ST=London,L=London,O=Home Office,CN=automated-test-client")
    mockMvc
      .perform(
        post("/v1/prison/$prisonId/prisoners/$hmppsId/transactions").headers(headers).content(requestBody).contentType(org.springframework.http.MediaType.APPLICATION_JSON),
      ).andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("transaction-create-response")))
  }

  // POST transfer transaction
  @Test
  fun `return an expected response for a successful transaction transfer post`() {
    val requestBody =
      """
      {
        "description": "Canteen Purchase of £16.34",
        "amount": 1634,
        "clientTransactionId": "CL123212",
        "clientUniqueRef": "CLIENT121131-0_11"
      }
      """.trimIndent()

    val headers = org.springframework.http.HttpHeaders()
    headers.set("subject-distinguished-name", "C=GB,ST=London,L=London,O=Home Office,CN=automated-test-client")
    mockMvc
      .perform(
        post("/v1/prison/$prisonId/prisoners/$hmppsId/transactions/transfer").headers(headers).content(requestBody).contentType(org.springframework.http.MediaType.APPLICATION_JSON),
      ).andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("transaction-transfer-create-response")))
  }
}
