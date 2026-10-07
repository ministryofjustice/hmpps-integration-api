package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.prison

import org.junit.jupiter.api.Test
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase

class BalanceIntegrationTest : IntegrationTestBase() {
  private final val accountCode = "savings"
  private final val balancePrisonPath = "/v1/prison/$prisonId/prisoners/$nomsId/balances"
  private final val accountCodePrisonPath = "/v1/prison/$prisonId/prisoners/$nomsId/accounts/$accountCode/balances"

  @Test
  fun `return a list of a prisoner's balances`() {
    callApi(balancePrisonPath)
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("person-balances.json"), JsonCompareMode.STRICT))
  }

  @Test
  fun `return a single balance for a prisoner given an account code`() {
    callApi(accountCodePrisonPath)
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("person-balance.json"), JsonCompareMode.STRICT))
  }
}
