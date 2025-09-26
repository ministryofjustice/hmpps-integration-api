package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase
import java.time.LocalDateTime

class CaseNotesIntegrationTest : IntegrationTestBase() {
  private final val startDate: LocalDateTime = LocalDateTime.now()
  private final val endDate: LocalDateTime = LocalDateTime.now()
  private final val cnMatchedPrisonerId = nomsId
  private final val path = "$basePath/$cnMatchedPrisonerId/case-notes"

  @Test
  fun `returns case notes for a person`() {
    callApi(path)
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("person-case-notes")))
  }

  @Test
  fun `returns case notes for a person when date range query`() {
    callApi(
      "$path?startDate=$startDate&endDate=$endDate",
    ).andExpect(status().isOk)
  }

  @Test
  fun `returns a 400 if the hmppsId is invalid`() {
    callApi("$basePath/$invalidNomsId/case-notes")
      .andExpect(status().isBadRequest)
  }

  @Test
  fun `returns a 404 for if consumer has empty list of prisons`() {
    callApiWithCN(path, noPrisonsCn)
      .andExpect(status().isNotFound)
  }

  @Test
  fun `returns a 404 for prisoner in wrong prison`() {
    callApiWithCN(path, limitedPrisonsCn)
      .andExpect(status().isNotFound)
  }

  @Test
  fun `returns specific case note types for a person`() {
    callApiWithCN(path, limitedCaseNotesCn)
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("person-case-notes")))
  }
}
