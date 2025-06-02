package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase

class PLPIntegrationTest : IntegrationTestBase() {
//  @Disabled("Prism generated mock API is returning bad data due to generated schema from other project")
  @Test
  fun `returns a persons induction schedule`() {
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
  }
}
