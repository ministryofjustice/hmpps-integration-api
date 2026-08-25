package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.v2

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.PERSON_SEARCH_V2_ENABLED
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase
import java.io.File

class PersonSearchIntegrationTest : IntegrationTestBase() {
  val path = "/v2/persons"
  val request =
    File(
      "$gatewaysFolder/cpr/fixtures/core-person-record-search-request.json",
    ).readText()

  val invalidRequest =
    File(
      "$gatewaysFolder/cpr/fixtures/core-person-record-search-invalid-request.json",
    ).readText()

  val response =
    File(
      "$gatewaysFolder/cpr/fixtures/core-person-record-search-response.json",
    ).readText()

  @BeforeEach
  fun setup() {
    corePersonRecordMockServer.stubForPost(
      "/person/search",
      resBody = response,
      reqBody = request,
    )
  }

  @Test
  fun `successfully searches for a person`() {
    postToApi(path, request)
      .andExpect(status().isOk)
      .andExpect(MockMvcResultMatchers.content().json(response))
  }

  @Test
  fun `upstream returns a 404 for a person search`() {
    corePersonRecordMockServer.stubForPost(
      "/person/search",
      resBody = "",
      reqBody = invalidRequest,
      status = HttpStatus.NOT_FOUND,
    )

    postToApi(path, invalidRequest)
      .andExpect(status().isNotFound)
  }

  @Test
  fun `upstream returns a 400 for a person search`() {
    corePersonRecordMockServer.stubForPost(
      "/person/search",
      resBody = "",
      reqBody = invalidRequest,
      status = HttpStatus.BAD_REQUEST,
    )

    postToApi(path, invalidRequest)
      .andExpect(status().isBadRequest)
  }

  @Test
  fun `returns a 503 for person search when v2 person search not enabled`() {
    whenever(featureFlagConfig.getConfigFlagValue(PERSON_SEARCH_V2_ENABLED)).thenReturn(false)
    postToApi(path, request)
      .andExpect(status().isServiceUnavailable)
  }
}
