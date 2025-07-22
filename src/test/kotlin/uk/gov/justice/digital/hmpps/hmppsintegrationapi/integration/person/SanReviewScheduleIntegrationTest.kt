package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import org.junit.jupiter.api.Test
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
      .andExpect(
        content().json(
          """
           {
            "data":
            {
              "reviewSchedules": [
                {
                  "reference": "628490ec-b944-4813-bdee-a2a6fe3e7d88",
                  "deadlineDate": "2025-07-21",
                  "status": "SCHEDULED",
                  "createdBy": "SMCALLISTER_GEN",
                  "createdByDisplayName": "Stephen Mcallister",
                  "createdAt": "2025-07-21T12:18:53.875633Z",
                  "createdAtPrison": "MDI",
                  "updatedBy": "SMCALLISTER_GEN",
                  "updatedByDisplayName": "Stephen Mcallister",
                  "updatedAt": "2025-07-21T12:18:53.875648Z",
                  "updatedAtPrison": "MDI",
                  "reviewCompletedDate": null,
                  "reviewKeyedInBy": null,
                  "reviewCompletedBy": null,
                  "reviewCompletedByJobRole": null,
                  "exemptionReason": null,
                  "version": 1
                }
              ]
            }
          }
      """,
        ),
      )
  }
}
