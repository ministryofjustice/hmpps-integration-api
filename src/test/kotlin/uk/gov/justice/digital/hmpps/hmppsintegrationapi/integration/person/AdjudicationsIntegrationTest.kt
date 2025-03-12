package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import org.junit.jupiter.api.Test
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase

class AdjudicationsIntegrationTest : IntegrationTestBase() {
  @Test
  fun `returns adjudications for a person`() {
    callApi("$basePath/$nomsId/reported-adjudications")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("person-adjudications"), JsonCompareMode.STRICT))
  }

  @Test
  fun `adjudications returns a 400 if the hmppsId is invalid`() {
    callApi("$basePath/$invalidNomsId/reported-adjudications")
      .andExpect(status().isBadRequest)
  }

  @Test
  fun `return a 404 for person in wrong prison`() {
    callApiWithCN("$basePath/$nomsId/reported-adjudications", limitedPrisonsCn)
      .andExpect(status().isNotFound)
  }

  @Test
  fun `return a 404 when no prisons in filter`() {
    callApiWithCN("$basePath/$nomsId/reported-adjudications", noPrisonsCn)
      .andExpect(status().isNotFound)
  }
}
