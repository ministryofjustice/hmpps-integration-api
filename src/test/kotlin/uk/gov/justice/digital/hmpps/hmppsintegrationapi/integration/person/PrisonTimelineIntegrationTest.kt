package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase

class PrisonTimelineIntegrationTest : IntegrationTestBase() {
  private final val path = "$basePath/$nomsId/prison-timeline"
  private final val probationPath = "$basePath/$nomsIdFromProbation/prison-timeline"

  @Test
  fun `returns prison timeline for a person`() {
    callApi(path)
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("prison-timeline.json")))
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
  fun `returns a 404 for prisoner on probation`() {
    callApiWithCN(probationPath, noProbationAccessCn)
      .andExpect(status().isNotFound)
  }

  @Test
  fun `returns specific case note types for a person`() {
    callApiWithCN(path, limitedCaseNotesCn)
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("prison-timeline.json")))
  }
}
