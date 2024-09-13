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
}
