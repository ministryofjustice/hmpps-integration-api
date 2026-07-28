package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import kotlin.test.Test

class CourtReferenceIntegrationTest : IntegrationTestBase() {
  private final val path = "/v1/hmpps/reference-data/courts/ACCRYC"

  @Test
  fun `can get court when pass down a court id`() {
    callApi(
      path,
    ).andExpect(status().isOk)
      .andExpect(
        content().json(
          """
          {
            "data":
              {
                "courtName":"Accrington Youth Court"
              }
          }
          """.trimIndent(),
        ),
      )

    courtRegisterMockServer.assertValidationPassed()
  }

  @Test
  fun `returns a 404 from court`() {
    courtRegisterMockServer.stubForGet(
      "/courts/id/ACCRYC",
      "",
      HttpStatus.NOT_FOUND,
    )
    callApi(
      path,
    ).andExpect(status().isNotFound)
  }

  @Test
  fun `returns a 500 from court`() {
    courtRegisterMockServer.stubForGet(
      "/courts/id/ACCRYC",
      "",
      HttpStatus.INTERNAL_SERVER_ERROR,
    )
    callApi(
      path,
    ).andExpect(status().isInternalServerError)
  }
}
