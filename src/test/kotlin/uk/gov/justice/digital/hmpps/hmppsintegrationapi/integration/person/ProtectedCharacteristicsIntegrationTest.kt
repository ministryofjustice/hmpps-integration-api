package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase

class ProtectedCharacteristicsIntegrationTest : IntegrationTestBase() {
  @Test
  fun `returns protected characteristics for a person`() {
    callApi("$basePath/$pnc/protected-characteristics")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("person-protected-characteristics")))
  }
}
