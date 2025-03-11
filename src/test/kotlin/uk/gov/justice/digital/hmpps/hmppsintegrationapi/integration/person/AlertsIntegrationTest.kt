package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase

class AlertsIntegrationTest : IntegrationTestBase() {
  final var path = "$basePath/$nomsId/alerts"

  @Test
  fun `returns alerts for a person`() {
    callApi(path)
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("person-alerts")))
  }

  @Test
  fun `return a 404 for person in wrong prison`() {
    callApiWithCN(path, limitedPrisonsCn)
      .andExpect(status().isNotFound)
  }

  @Test
  fun `return a 404 when no prisons in filter`() {
    callApiWithCN(path, noPrisonsCn)
      .andExpect(status().isNotFound)
  }

  @Test
  fun `returns PND alerts for a person`() {
    callApi("$path/pnd")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("person-alerts-pnd")))
  }
}
