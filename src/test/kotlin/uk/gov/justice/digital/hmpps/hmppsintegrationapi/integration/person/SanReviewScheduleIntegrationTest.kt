package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import org.junit.jupiter.api.Test
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase
import java.io.File

class SanReviewScheduleIntegrationTest : IntegrationTestBase() {
  @Test
  fun `returns a persons san review schedule`() {
    sanMockServer.stubForGet(
      "/profile/$nomsId/reviews/review-schedules?includeAllHistory=true",
      File(
        "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/san/fixtures/GetSANReviewScheduleResponse.json",
      ).readText(),
    )

    callApi("$basePath/$nomsId/education/san/review-schedule")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("person-review-schedules.json"), JsonCompareMode.STRICT))
  }
}
