package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.prison

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase

class BalanceIntegrationTest : IntegrationTestBase() {
  private final val hmppsId = "G2996UX"
  private final val prisonId = "ABC"
  private final val basePrisonPath = "/v1/prison/$prisonId/prisoners/$hmppsId/balances"

  @Test
  fun `return a list of a prisoner's balances`() {
    callApi(basePrisonPath)
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
}
