package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import org.junit.jupiter.api.Test
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase

class VisitOrdersIntegrationTest : IntegrationTestBase() {
  @Test
  fun `returns visit orders for a person`() {
    callApi("$basePath/$nomsId/visit-orders")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("person-visit-orders.json"), JsonCompareMode.STRICT))
  }

  @Test
  fun `return a 404 for person in wrong prison`() {
    callApiWithCN("$basePath/$nomsId/visit-orders", limitedPrisonsCn)
      .andExpect(status().isNotFound)
  }

  @Test
  fun `return a 400 when invalid noms passed in`() {
    callApi("$basePath/$invalidNomsId/visit-orders")
      .andExpect(status().isBadRequest)
  }
}
