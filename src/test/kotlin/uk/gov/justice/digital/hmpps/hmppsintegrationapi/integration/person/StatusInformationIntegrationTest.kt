package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase

class StatusInformationIntegrationTest : IntegrationTestBase() {
  @Test
  fun `returns status information for a person`() {
    callApi("$basePath/$pnc/status-information")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("person-status-information")))
  }
}
