package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase

class HealthAndDietIntegrationTest : IntegrationTestBase() {
  @Test
  fun `returns health and diet information for a person`() {
    callApi("$basePath/$nomsId/health-and-diet")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("person-health-and-diet")))
  }

  @Test
  fun `return a 404 for person in wrong prison`() {
    callApiWithCN("$basePath/$nomsId/health-and-diet", limitedPrisonsCn)
      .andExpect(status().isNotFound)
  }

  @Test
  fun `return a 404 when no prisons in filter`() {
    callApiWithCN("$basePath/$nomsId/health-and-diet", noPrisonsCn)
      .andExpect(status().isNotFound)
  }

  @Test
  fun `return a 400 when invalid noms passed in`() {
    callApi("$basePath/$invalidNomsId/health-and-diet")
      .andExpect(status().isBadRequest)
  }
}
