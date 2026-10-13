package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase
import java.io.File

class PLPIntegrationTest : IntegrationTestBase() {
  @AfterEach
  fun resetValidators() {
    plpMockServer.resetValidator()
  }

  @Test
  fun `returns a persons induction schedule`() {
    plpMockServer.stubForGet(
      "/inductions/$nomsId/induction-schedule",
      File(
        "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/plp/fixtures/GetInductionScheduleResponse.json",
      ).readText(),
    )

    callApi("$basePath/$nomsId/plp-induction-schedule")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("person-plp-induction-schedule.json"), JsonCompareMode.STRICT))
    plpMockServer.assertValidationPassed()
  }

  @Test
  fun `returns a persons induction schedule history`() {
    plpMockServer.stubForGet(
      "/inductions/$nomsId/induction-schedule/history",
      File(
        "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/plp/fixtures/GetInductionScheduleHistoryResponse.json",
      ).readText(),
    )

    callApi("$basePath/$nomsId/plp-induction-schedule/history")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("person-plp-induction-schedule-history.json"), JsonCompareMode.STRICT))

    plpMockServer.assertValidationPassed()
  }

  @Test
  fun `returns a persons review schedule history`() {
    plpMockServer.stubForGet(
      "/action-plans/$nomsId/reviews/review-schedules",
      File(
        "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/plp/fixtures/GetReviewSchedulesResponse.json",
      ).readText(),
    )
    plpMockServer.stubForGet(
      "/action-plans/$nomsId/reviews",
      File(
        "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/plp/fixtures/GetReviewsResponse.json",
      ).readText(),
    )

    callApi("$basePath/$nomsId/plp-review-schedule")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("plp-review-schedule-history.json"), JsonCompareMode.STRICT))

    plpMockServer.assertValidationPassed()
  }
}
