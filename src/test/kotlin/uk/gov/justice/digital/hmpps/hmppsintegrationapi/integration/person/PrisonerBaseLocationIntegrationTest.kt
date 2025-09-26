package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase

class PrisonerBaseLocationIntegrationTest : IntegrationTestBase() {
  private val knownNomisNumber = nomsId
  private val unknownNomisNumber = "Z9876YX"

  @Test
  fun `returns prisoner base location, having hmppsId being nomisID`() {
    callApi(path = makePathPrisonerBaseLocation(knownNomisNumber))
      .andExpect(status().isOk)
      .andExpect(
        content().json(getExpectedResponse("prisoner-base-location")),
      )
  }

  @Test
  fun `does not return prisoner base location, of an unknown prisoner`() {
    callApi(path = makePathPrisonerBaseLocation(unknownNomisNumber))
      .andExpect(status().isNotFound)
  }

  @Test
  fun `returns a 400 if the hmppsId is invalid`() {
    callApi(path = makePathPrisonerBaseLocation(invalidNomsId))
      .andExpect(status().isBadRequest)
  }

  @Test
  fun `return a 404 for person in wrong prison`() {
    callApiWithCN(makePathPrisonerBaseLocation(knownNomisNumber), limitedPrisonsCn)
      .andExpect(status().isNotFound)
  }

  @Test
  fun `return a 404 when no prisons in filter`() {
    callApiWithCN(makePathPrisonerBaseLocation(knownNomisNumber), noPrisonsCn)
      .andExpect(status().isNotFound)
  }

  private fun makePathPrisonerBaseLocation(hmppsId: String) = "$basePath/$hmppsId/prisoner-base-location"
}
