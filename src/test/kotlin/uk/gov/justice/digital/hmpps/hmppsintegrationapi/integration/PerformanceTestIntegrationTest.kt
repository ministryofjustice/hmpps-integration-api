package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
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
}
