package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import org.junit.jupiter.api.Test
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase
import java.io.File

class SanPlanCreationScheduleIntegrationTest : IntegrationTestBase() {
  @Test
  fun `returns a persons san plan creation schedule`() {
    sanMockServer.stubForGet(
      "/profile/$nomsId/plan-creation-schedule?includeAllHistory=true",
      File(
        "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/san/fixtures/GetSANPlanCreationScheduleResponse.json",
      ).readText(),
    )

    callApi("$basePath/$nomsId/education/san/plan-creation-schedule")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("person-plan-creation-schedule.json"), JsonCompareMode.STRICT))
  }
}
