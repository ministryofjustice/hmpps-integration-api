package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate

class TransactionsIntegrationTest : IntegrationTestBase() {
  val accountCode = "spends"
  final val fromDate = "2024-01-01"
  final val toDate = "2024-01-14"
  var dateQueryParams = "?from_date=$fromDate&to_date=$toDate"

  @BeforeEach
  fun setUp() {
    mockkStatic(LocalDate::class)
    every { LocalDate.now() } returns LocalDate.of(2026, 10, 7)
  }

  @AfterEach
  fun tearDown() {
    unmockkStatic(LocalDate::class)
  }

  @Test
  fun `return a list of transactions for a prisoner`() {
    callApi("/v1/prison/$prisonId/prisoners/$nomsId/accounts/$accountCode/transactions")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("transactions-response.json"), JsonCompareMode.STRICT))
  }

  @Test
  fun `return a list of transactions when the dates are supplied in the request`() {
    callApi("/v1/prison/$prisonId/prisoners/$nomsId/accounts/$accountCode/transactions$dateQueryParams")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("transactions-response.json"), JsonCompareMode.STRICT))
  }

  @Test
  fun `transactions returns 404`() {
    val wrongPrisonId = "XYZ"
    callApiWithCN("/v1/prison/$wrongPrisonId/prisoners/$nomsId/accounts/$accountCode/transactions", limitedPrisonsCn)
      .andExpect(status().isNotFound)
  }

  // transaction
  @Test
  fun `return a transaction for a prisoner`() {
    callApi("/v1/prison/$prisonId/prisoners/$nomsId/transactions/$clientReference")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("transaction-response.json"), JsonCompareMode.STRICT))
  }

  @Test
  fun `transaction returns 404`() {
    val wrongPrisonId = "XYZ"
    callApiWithCN("/v1/prison/$wrongPrisonId/prisoners/$nomsId/transactions/$clientReference", limitedPrisonsCn)
      .andExpect(status().isNotFound)
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

    postToApi("/v1/prison/$prisonId/prisoners/$nomsId/transactions", requestBody)
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("transaction-create-response.json"), JsonCompareMode.STRICT))
  }

  @Test
  fun `does throw 403 when empty prison field in consumer profile`() {
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

    postToApiWithCN("/v1/prison/$prisonId/prisoners/$nomsId/transactions", requestBody, emptyPrisonsCn)
      .andExpect(status().isForbidden)
  }

  // POST transaction transfer

  @Test
  fun `return an expected response for a successful transaction transfer post`() {
    val requestBody =
      """
      {
        "description": "Canteen Purchase of £16.34",
        "amount": 1634,
        "clientTransactionId": "CL123212",
        "clientUniqueRef": "CLIENT121131-0_11",
        "fromAccount": "spends",
        "toAccount": "savings"
      }
      """.trimIndent()

    postToApi("/v1/prison/$prisonId/prisoners/$nomsId/transactions/transfer", requestBody)
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("transaction-transfer-create-response.json"), JsonCompareMode.STRICT))
  }

  @Test
  fun `return a bad request for a transaction transfer post with invalid from or to accounts`() {
    val requestBody =
      """
      {
        "description": "Canteen Purchase of £16.34",
        "amount": 1634,
        "clientTransactionId": "CL123212",
        "clientUniqueRef": "CLIENT121131-0_11",
        "fromAccount": "wrong",
        "toAccount": "wrong"
      }
      """.trimIndent()

    postToApi("/v1/prison/$prisonId/prisoners/$nomsId/transactions/transfer", requestBody)
      .andExpect(status().isBadRequest)
  }
}
