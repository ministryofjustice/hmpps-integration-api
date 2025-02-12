package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase

class VisitRestrictionIntegrationTest : IntegrationTestBase() {
  @Test
  fun `returns visit restrictions for a person`() {
    callApi("$basePath/$nomsId/visit-restrictions")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("person-visit-restrictions")))
  }

  @Test
  fun `return a 404 for person in wrong prison`() {
    callApiWithCN("$basePath/$nomsId/visit-restrictions", limitedPrisonsCn)
      .andExpect(status().isNotFound)
  }

  @Test
  fun `return a 404 when no prisons in filter`() {
    callApiWithCN("$basePath/$nomsId/visit-restrictions", noPrisonsCn)
      .andExpect(status().isNotFound)
  }

  @Test
  fun `return a 400 when invalid noms passed in`() {
    callApi("$basePath/$invalidNomsId/visit-restrictions")
      .andExpect(status().isBadRequest)
  }
}
