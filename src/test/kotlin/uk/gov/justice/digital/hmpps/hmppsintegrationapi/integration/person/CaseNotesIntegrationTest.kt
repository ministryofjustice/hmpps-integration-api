package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase

class CaseNotesIntegrationTest : IntegrationTestBase() {
  @Test
  fun `returns case notes for a person`() {
    callApi("$basePath/$crn/case-notes")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("person-case-notes")))
  }

  @Test
  fun `returns a 400 if the hmppsId is invalid`() {
    callApi("$basePath/$invalidNomsId/case-notes")
      .andExpect(status().isBadRequest)
  }

  @Test
  fun `returns a 404 for if consumer has empty list of prisons`() {
    callApiWithCN("$basePath/$crn/case-notes", noPrisonsCn)
      .andExpect(status().isNotFound)
  }

  @Test
  fun `returns a 404 for prisoner in wrong prison`() {
    callApiWithCN("$basePath/$crn/case-notes", limitedPrisonsCn)
      .andExpect(status().isNotFound)
  }
}
