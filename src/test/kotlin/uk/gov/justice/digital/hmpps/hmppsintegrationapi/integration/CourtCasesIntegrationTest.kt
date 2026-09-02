package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import org.springframework.http.HttpStatus
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.io.File
import kotlin.test.Test

class CourtCasesIntegrationTest : IntegrationTestBase() {
  @Test
  fun `can get court cases summary when passing a crn`() {
    callApi(
      "/v1/persons/$crn/court-cases",
    ).andExpect(status().isOk)
      .andExpect(
        MockMvcResultMatchers.content().json(
          File("src/test/resources/expected-responses/court-cases.json").readText(),
          JsonCompareMode.STRICT,
        ),
      )

    remandAndSentencingMockServer.assertValidationPassed()
  }

  @Test
  fun `returns a 400 caused by an invalid hmppsId`() {
    callApi(
      "/v1/persons/invalid/court-cases",
    ).andExpect(status().isBadRequest)
  }

  @Test
  fun `returns a 404 from remand and sentencing`() {
    remandAndSentencingMockServer.stubForGet(
      "/person/$nomsId/sentenced-court-cases",
      "",
      HttpStatus.NOT_FOUND,
    )
    callApi(
      "/v1/persons/$crn/court-cases",
    ).andExpect(status().isNotFound)
  }

  @Test
  fun `returns a 500 from remand and sentencing`() {
    remandAndSentencingMockServer.stubForGet(
      "/person/$nomsId/sentenced-court-cases",
      "",
      HttpStatus.INTERNAL_SERVER_ERROR,
    )
    callApi(
      "/v1/persons/$crn/court-cases",
    ).andExpect(status().isInternalServerError)
  }
}
