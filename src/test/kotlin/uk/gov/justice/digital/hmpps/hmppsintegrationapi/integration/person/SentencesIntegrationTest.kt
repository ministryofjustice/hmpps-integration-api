package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase

class SentencesIntegrationTest :  IntegrationTestBase() {

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
}
