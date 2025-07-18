package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
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
      .andExpect(
        content().json(
          """
           {"data": {
            "deadlineDate":"2025-03-20",
            "status":"SCHEDULED",
            "calculationRule": "NEW_PRISON_ADMISSION",
            "nomisNumber": "X1234YZ",
            "systemUpdatedBy":"system",
            "systemUpdatedAt":"2025-02-28T10:25:34.949Z"}}
      """,
        ),
      )

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
      .andExpect(
        content().json(
          """
           {"data":
            {"inductionSchedules":
              [
              {
              "deadlineDate":"2025-03-20",
              "status":"SCHEDULED",
              "calculationRule":"NEW_PRISON_ADMISSION",
              "nomisNumber":"X1234YZ",
              "systemCreatedBy":"system",
              "systemCreatedAt":"2025-02-28T10:25:34.949Z",
              "systemCreatedAtPrison":"MKI",
              "systemUpdatedBy":"system",
              "systemUpdatedAt":"2025-02-28T10:25:34.949Z",
              "systemUpdatedAtPrison":"MKI",
              "inductionPerformedBy":null,
              "inductionPerformedAt":null,
              "inductionPerformedByRole":null,
              "inductionPerformedAtPrison":null,
              "exemptionReason":null,
              "version":1}]}}
      """,
        ),
      )

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
      .andExpect(content().json(getExpectedResponse("plp-review-schedule-history")))

    plpMockServer.assertValidationPassed()
  }
}
