package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase

class AlertsIntegrationTest : IntegrationTestBase() {
  final var path = "$basePath/$pnc/alerts"

  @Test
  fun `returns alerts for a person`() {
    callApi(path)
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("person-alerts")))
  }

  @Test
  fun `returns PND alerts for a person`() {
    callApi("$path/pnd")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("person-alerts-pnd")))
  }
}
