package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase

class SentencesIntegrationTest : IntegrationTestBase() {
  final var path = "$basePath/$pnc/sentences"

  @Test
  fun `returns sentences for a person`() {
    callApi(path)
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("person-sentence")))
  }

  @Test
  fun `returns latest sentence key dates and adjustments for a person`() {
    callApi("$path/latest-key-dates-and-adjustments")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("person-sentence-key-dates")))
  }

  @Test
  fun `return a 404 for if consumer has empty list of prisons on latest sentence key dates and adjustments `() {
    callApiWithCN("$path/latest-key-dates-and-adjustments", noPrisonsCn)
      .andExpect(status().isNotFound)
  }

  @Test
  fun `return a 404 for prisoner in wrong prison on latest sentence key dates and adjustments`() {
    callApiWithCN("$path/latest-key-dates-and-adjustments", limitedPrisonsCn)
      .andExpect(status().isNotFound)
  }
}
