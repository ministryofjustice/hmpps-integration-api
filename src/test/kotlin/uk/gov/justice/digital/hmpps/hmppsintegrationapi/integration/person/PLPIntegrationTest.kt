package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase

class PLPIntegrationTest : IntegrationTestBase() {
  @Test
  fun `returns a persons integration schedule`() {
    callApi("$basePath/K5995YZ/plp-induction-schedule")
      .andExpect(status().isOk)
      .andExpect(
        content().json(
          """
           {"data": {
            "deadlineDate":"2019-08-24",
            "status":"SCHEDULED",
            "calculationRule": "NEW_PRISON_ADMISSION",
            "nomisNumber": "A1234BC",
            "systemUpdatedBy":"Alex Smith",
            "systemUpdatedAt":"2023-06-19T09:39:44Z"}}
      """,
        ),
      )
  }
}
