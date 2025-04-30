package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.prison

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase

class PrisonCapacityIntegrationTest : IntegrationTestBase() {
  private val prisonId = "MDI"
  private val path = "/v1/prison/$prisonId/capacity"

  @Test
  fun `return the prison capacity details`() {
    callApi(path)
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("prison-capacity-response")))
  }

  @Test
  fun `return a 404 when prison not in the allowed prisons`() {
    callApiWithCN(path, limitedPrisonsCn)
      .andExpect(status().isNotFound)
  }

  @Test
  fun `return a 404 no prisons in filter`() {
    callApiWithCN(path, noPrisonsCn)
      .andExpect(status().isNotFound)
  }
}
