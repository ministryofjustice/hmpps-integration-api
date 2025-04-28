package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.prison

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase

class ResidentialHierarchyIntegrationTest : IntegrationTestBase() {
  private val prisonId = "MDI"
  private val path = "/v1/prison/$prisonId/residential-hierarchy"

  @Test
  fun `return a prisoner with all fields populated`() {
    callApi(path)
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("prison-residential-hierarchy-response")))
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
