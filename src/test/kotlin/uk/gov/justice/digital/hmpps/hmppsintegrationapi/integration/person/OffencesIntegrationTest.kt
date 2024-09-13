package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase

class OffencesIntegrationTest : IntegrationTestBase() {
  @Test
  fun `returns offences for a person`() {
    callApi("$basePath/$pnc/offences")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("person-offences")))
  }
}
