package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase

class PLPIntegrationTest : IntegrationTestBase() {
  @Test
  fun `returns person cell location if in prison`() {
    callApi("$basePath/K5995YZ/plp/inductionScheduleUpdated")
      .andExpect(status().isOk)
      .andExpect(
        content().json(
          """
           {"data": {
            "deadlineDate":"2019-08-24",
            "scheduleStatus":"SCHEDULED",
            "scheduleCalculationRule": "NEW_PRISON_ADMISSION",
            "prisonNumber": "A1234BC"}}
      """,
        ),
      )
  }
}
