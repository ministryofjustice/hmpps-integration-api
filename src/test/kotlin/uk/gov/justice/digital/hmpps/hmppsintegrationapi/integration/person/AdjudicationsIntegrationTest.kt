package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase

class AdjudicationsIntegrationTest : IntegrationTestBase() {
  @Test
  fun `returns adjudications for a person`() {
    callApi("$basePath/$nomsId/reported-adjudications")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("person-adjudications"), true))
  }
}
