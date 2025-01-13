package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.prison

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase

class BalanceIntegrationTest : IntegrationTestBase() {
  private final val hmppsId = "G2996UX"
  private final val prisonId = "ABC"
  private final val accountCode = "savings"
  private final val balancePrisonPath = "/v1/prison/$prisonId/prisoners/$hmppsId/balances"
  private final val accountCodePrisonPath = "/v1/prison/$prisonId/prisoners/$hmppsId/balances/$accountCode"

  @Test
  fun `return a list of a prisoner's balances`() {
    callApi(balancePrisonPath)
      .andExpect(status().isOk)
      .andExpect(
        content().json(
          """
        {
          "data": {
            "balances": [
              {
                "accountCode": "spends",
                "amount": 5678
              },
              {
                "accountCode": "savings",
                "amount": 12344
              },
              {
                "accountCode": "cash",
                "amount": 13565
              }
            ]
          }
        }
      """,
        ),
      )
  }

  @Test
  fun `return a single balance for a prisoner given an account code`() {
    callApi(accountCodePrisonPath)
      .andExpect(status().isOk)
      .andExpect(
        content().json(
          """
        {
          "data": {
            "balances": [
              {
                "accountCode": "savings",
                "amount": 12344
              }
            ]
          }
        }
      """,
        ),
      )
  }
}
