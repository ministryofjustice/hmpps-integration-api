package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase

class NeedsIntegrationTest : IntegrationTestBase() {
  @Test
  fun `returns needs for a person`() {
    callApi("$basePath/$pnc/needs")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("person-needs")))
  }
}
