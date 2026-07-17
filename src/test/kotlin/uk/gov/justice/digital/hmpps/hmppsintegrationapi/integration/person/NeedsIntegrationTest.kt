package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase
import java.io.File

@TestPropertySource(properties = ["services.assess-risks-and-needs.base-url=http://localhost:4032"])
class NeedsIntegrationTest : IntegrationTestBase() {
  @BeforeEach
  fun setUp() {
    arnsMockServer.start()
    arnsMockServer.stubForGet(
      "/needs/$crn",
      File(
        "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/assessrisksandneeds/fixtures/GetNeedsResponse.json",
      ).readText(),
    )
  }

  @AfterEach
  fun tearDown() {
    arnsMockServer.stop()
    arnsMockServer.resetValidator()
  }

  @Test
  fun `returns needs for a person`() {
    callApi("$basePath/$crn/needs")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("person-needs.json")))
  }
}
