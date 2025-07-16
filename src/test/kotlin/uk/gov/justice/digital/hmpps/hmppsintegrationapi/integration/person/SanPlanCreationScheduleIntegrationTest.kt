package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import org.junit.jupiter.api.Test
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

    callApi("$basePath/$nomsId/san-plan-creation-schedule")
      .andExpect(status().isOk)
      .andExpect(
        content().json(
          """
           {
            "data": {
              "planCreationSchedules": [
                  {
                      "reference": "44052fd9-bf6c-41bc-8308-6839a7048836",
                      "status": "SCHEDULED",
                      "createdBy": "system",
                      "createdByDisplayName": "system",
                      "createdAt": "2025-07-16T08:48:11.844724Z",
                      "createdAtPrison": "BXI",
                      "updatedBy": "system",
                      "updatedByDisplayName": "system",
                      "updatedAt": "2025-07-16T08:48:11.844737Z",
                      "updatedAtPrison": "BXI",
                      "deadlineDate": "2025-10-06",
                      "exemptionReason": null,
                      "exemptionDetail": null,
                      "needSources": [
                          "ALN_SCREENER",
                          "LDD_SCREENER"
                      ],
                      "version": 1
                  },
                  {
                      "reference": "44052fd9-bf6c-41bc-8308-6839a7048836",
                      "status": "EXEMPT_PRISONER_NOT_COMPLY",
                      "createdBy": "system",
                      "createdByDisplayName": "system",
                      "createdAt": "2025-07-16T08:48:11.844724Z",
                      "createdAtPrison": "BXI",
                      "updatedBy": "SMCALLISTER_GEN",
                      "updatedByDisplayName": "Stephen Mcallister",
                      "updatedAt": "2025-07-16T08:48:29.143136Z",
                      "updatedAtPrison": "MDI",
                      "deadlineDate": null,
                      "exemptionReason": "EXEMPT_REFUSED_TO_ENGAGE",
                      "exemptionDetail": "aa",
                      "needSources": [
                          "ALN_SCREENER",
                          "LDD_SCREENER"
                      ],
                      "version": 2
                  }
              ]
            }
          }
      """,
        ),
      )
  }
}
