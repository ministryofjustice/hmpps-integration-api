package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class PerformanceTestIntegrationTest : IntegrationTestBase() {
  @Test
  fun `return a list of transactions for a prisoner`() {
    callApi("/v1/performance-test/test-1/ABC/A1234AA")
      .andExpect(status().isOk)
      .andExpect(
        content().json(
          """
          {
          "data": {
            "balances": [
              {
                "accountCode": "spends",
                "amount": 123
              },
              {
                "accountCode": "savings",
                "amount": 456
              },
              {
                "accountCode": "cash",
                "amount": 789
              }
            ]
          }
        }
        """,
        ),
      )
  }

  @Test
  fun `return a 200 for empty but successful upstream response`() {
    callApi("/v1/performance-test/test-2?first_name=Tom")
      .andExpect(status().isOk)
  }

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
        post("/v1/performance-test/test-3/ABC/A1234AA")
          .headers(headers)
          .content(requestBody)
          .contentType(org.springframework.http.MediaType.APPLICATION_JSON),
      ).andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("transaction-create-response")))
  }
}
