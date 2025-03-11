package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase

class SentencesIntegrationTest : IntegrationTestBase() {
  final var path = "$basePath/$nomsId/sentences"
  final var invalidHmppsIdPath = "$basePath/INVALID/sentences"

  @Test
  fun `returns sentences for a person`() {
    callApi(path)
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("person-sentence")))
  }

  @Test
  fun `sentences returns a 400 if the hmppsId is invalid`() {
    callApi(invalidHmppsIdPath)
      .andExpect(status().isBadRequest)
  }

  @Test
  fun `sentences returns a 404 for if consumer has empty list of prisons on latest sentence key dates and adjustments `() {
    callApiWithCN(path, noPrisonsCn)
      .andExpect(status().isNotFound)
  }

  @Test
  fun `sentences returns a 404 for prisoner in wrong prison on latest sentence key dates and adjustments`() {
    callApiWithCN(path, limitedPrisonsCn)
      .andExpect(status().isNotFound)
  }

  @Test
  fun `returns latest sentence key dates and adjustments for a person`() {
    callApi("$path/latest-key-dates-and-adjustments")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("person-sentence-key-dates")))
  }

  @Test
  fun `returns a 400 if the hmppsId is invalid`() {
    callApi("$invalidHmppsIdPath/latest-key-dates-and-adjustments")
      .andExpect(status().isBadRequest)
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
