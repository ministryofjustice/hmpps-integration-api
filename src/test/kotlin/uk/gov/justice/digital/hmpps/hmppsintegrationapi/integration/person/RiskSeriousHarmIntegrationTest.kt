package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase

class RiskSeriousHarmIntegrationTest : IntegrationTestBase() {
  @Test
  fun `returns serious harm risks for a person`() {
    callApi("$basePath/$crn/risks/serious-harm")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("risk-serious-harm.json")))
  }

  @Test
  fun `returns serious harm risks for a person for a nomis number`() {
    callApi("$basePath/$nomsId/risks/serious-harm")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("risk-serious-harm.json")))
  }

  @Test
  fun `returns not found when a crn cannot be resolved for the supplied nomis number`() {
    callApi("$basePath/A1234AB/risks/serious-harm")
      .andExpect(status().isNotFound)
  }

  @Test
  fun `returns forbidden for a person with lao redactions`() {
    callApiWithCN("$basePath/$nomsId/risks/serious-harm", "lao-role-based-redacted-client")
      .andExpect(status().isForbidden)
  }

  @Test
  fun `returns serious harm risks for a person with redactions`() {
    callApiWithCN("$basePath/$nomsIdFromProbation/risks/serious-harm", "police")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("risk-serious-harm-redacted.json")))
  }
}
