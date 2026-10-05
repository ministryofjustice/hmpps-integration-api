package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import org.junit.jupiter.api.Test
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase

class FutureVisitsIntegrationTest : IntegrationTestBase() {
  @Test
  fun `gets the future visits`() {
    callApi("$basePath/$nomsId/visit/future")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("visit-future-response.json"), JsonCompareMode.STRICT))
  }

  @Test
  fun `return a 404 when prison not in filter`() {
    callApiWithCN("$basePath/$nomsId/visit/future", limitedPrisonsCn)
      .andExpect(status().isNotFound)
  }

  @Test
  fun `return a 404 when no prisons in filter`() {
    callApiWithCN("$basePath/$nomsId/visit/future", noPrisonsCn)
      .andExpect(status().isNotFound)
  }
}
