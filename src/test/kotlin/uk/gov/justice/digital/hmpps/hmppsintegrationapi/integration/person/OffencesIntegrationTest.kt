package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase

class OffencesIntegrationTest : IntegrationTestBase() {
  @Test
  fun `returns offences for a person with filters and no prisons`() {
    /* e.g.
    automated-test-client:
      roles:
        - "full-access"
      filters:
     */
    callApiWithCN("$basePath/$nomsId/offences", cn = "automated-test-client")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("person-prison-probation-offences")))
  }

  // Should be exactly the same outcome as above
  @Test
  fun `returns offences for a person with no filters`() {
    /* e.g.
    automated-test-client-no-filters:
      roles:
        - "full-access"
     */
    callApiWithCN("$basePath/$nomsId/offences", cn = "automated-test-client-no-filters")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("person-prison-probation-offences")))
  }

  @Test
  fun `returns not found (404) for a person with empty prison filter`() {
    /* e.g.
    no-prisons:
      roles:
        - "full-access"
      filters:
        prisons:
     */
    callApiWithCN("$basePath/$nomsId/offences", cn = "no-prisons")
      .andExpect(status().isNotFound)
  }

  @Test
  fun `returns a 400 if the hmppsId is invalid`() {
    callApiWithCN("$basePath/$invalidNomsId/offences", cn = "specific-prison")
      .andExpect(status().isBadRequest)
  }

  @Test
  fun `returns a 404 for prisoner in wrong prison`() {
    callApiWithCN("$basePath/$nomsId/offences", limitedPrisonsCn)
      .andExpect(status().isNotFound)
  }
}
