package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase
import java.io.File

class NeedsIntegrationTest : IntegrationTestBase() {
  @BeforeEach
  fun setUp() {
    arnsMockServer.stubForGet(
      "/needs/$crn",
      File(
        "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/assessrisksandneeds/fixtures/GetNeedsResponse.json",
      ).readText(),
    )
  }

  @Test
  fun `returns needs for a person`() {
    callApi("$basePath/$crn/needs")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("person-needs.json"), JsonCompareMode.STRICT))
  }
}
