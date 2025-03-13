package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase

class RisksIntegrationTest : IntegrationTestBase() {
  @ParameterizedTest
  @ValueSource(strings = ["scores", "categories", "mappadetail", "dynamic", "serious-harm"])
  fun `returns protected characteristics for a person`(path: String) {
    callApi("$basePath/$crn/risks/$path")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("person-risk-$path")))
  }

  @ParameterizedTest
  @ValueSource(strings = ["categories"])
  fun `return a 404 when prison not in filter`(path: String) {
    callApiWithCN("$basePath/$crn/risks/$path", limitedPrisonsCn)
      .andExpect(status().isNotFound)
  }

  @ParameterizedTest
  @ValueSource(strings = ["categories"])
  fun `returns a 404 for prisoner in wrong prison`(path: String) {
    callApiWithCN("$basePath/$crn/risks/$path", noPrisonsCn)
      .andExpect(status().isNotFound)
  }

  @ParameterizedTest
  @ValueSource(strings = ["categories"])
  fun `return a 400 when invalid hmpps submitted`(path: String) {
    callApi("$basePath/invalid=invalid/risks/$path")
      .andExpect(status().isBadRequest)
  }
}
