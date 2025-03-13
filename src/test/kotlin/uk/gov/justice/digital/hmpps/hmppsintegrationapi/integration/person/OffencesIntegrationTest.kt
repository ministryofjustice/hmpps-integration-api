package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase

class OffencesIntegrationTest : IntegrationTestBase() {
  @Test
  fun `returns offences for a person`() {
    callApi("$basePath/$nomsId/offences")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("person-offences")))
  }

  @Test
  fun `returns a 400 if the hmppsId is invalid`() {
    callApi("$basePath/$invalidNomsId/offences")
      .andExpect(status().isBadRequest)
  }

  @Test
  fun `returns a 404 for if consumer has empty list of prisons`() {
    callApiWithCN("$basePath/$nomsId/offences", noPrisonsCn)
      .andExpect(status().isNotFound)
  }

  @Test
  fun `returns a 404 for prisoner in wrong prison`() {
    callApiWithCN("$basePath/$nomsId/offences", limitedPrisonsCn)
      .andExpect(status().isNotFound)
  }
}
