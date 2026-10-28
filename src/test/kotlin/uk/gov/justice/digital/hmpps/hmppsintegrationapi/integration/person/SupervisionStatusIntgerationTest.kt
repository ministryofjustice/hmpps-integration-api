package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import org.junit.jupiter.api.Test
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase
import java.io.File

class SupervisionStatusIntgerationTest : IntegrationTestBase() {
  @Test
  fun `returns supervision status for a person in prison`() {
    callApi("$basePath/$crn/supervision-status")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("supervision-status-in-prison.json"), JsonCompareMode.STRICT))
  }

  @Test
  fun `returns supervision status for a person on probation with community provider`() {
    nDeliusMockServer.stubForGet(
      "/case/$crnActiveInProbation/supervisions",
      File(
        "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/ndelius/fixtures/SupervisionsResponse.json",
      ).readText(),
    )
    callApi("$basePath/$crnActiveInProbation/supervision-status")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("supervision-status-on-probation.json"), JsonCompareMode.STRICT))
  }

  @Test
  fun `returns supervision status for a person on neither probation or in prison`() {
    callApi("$basePath/$crnNotActiveInPrisonOrProb/supervision-status")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("supervision-status-none.json"), JsonCompareMode.STRICT))
  }

  @Test
  fun `returns UNKNOWN supervision status for a person`() {
    callApi("$basePath/$crnUnknownInPrison/supervision-status")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("supervision-status-unknown.json"), JsonCompareMode.STRICT))
  }
}
