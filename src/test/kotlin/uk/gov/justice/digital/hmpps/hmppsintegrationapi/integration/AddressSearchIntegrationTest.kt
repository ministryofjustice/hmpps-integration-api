package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.AddressSearchRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.dsl.objectMapper
import java.io.File

class AddressSearchIntegrationTest : IntegrationTestBase() {
  val path = "/v1/address/search"
  val pathGetParams = "?buildingName=Burnham"
  val bodyPostParams =
    """
    {
      "buildingName":"Burnham"
    }
    """.trimIndent()

  val requestBody = AddressSearchRequest(buildingName = "Burnham")

  @BeforeEach
  fun setup() {
    val fixturesPath = "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/probationoffendersearch/fixtures/address-search-response.json"
    probationSearchMockServer.stubForPost(
      path,
      resBody =
        File(
          fixturesPath,
        ).readText(),
      reqBody = objectMapper.writeValueAsString(requestBody),
    )
  }

  @Test
  fun `successfully searches address building name using a GET`() {
    callApi("$path$pathGetParams")
      .andExpect(status().isOk)
      .andExpect(header().string("Cache-Control", "no-cache"))
  }

  @Test
  fun `address search returns a bad request when no search criteria using a GET`() {
    callApi(path)
      .andExpect(status().isBadRequest)
  }

  @Test
  fun `address search returns a 503 when feature flag is disabled using a GET`() {
    whenever(featureFlagConfig.getConfigFlagValue(FeatureFlagConfig.ADDRESS_SEARCH_ENDPOINT_ENABLED)).thenReturn(false)
    callApi(path)
      .andExpect(status().isServiceUnavailable)
  }

  @Test
  fun `successfully searches address building name using a POST`() {
    postToApi(path, bodyPostParams)
      .andExpect(status().isOk)
      .andExpect(header().string("Cache-Control", "no-cache"))
  }

  @Test
  fun `address search returns a bad request when no search criteria using a POST`() {
    postToApi(path, "")
      .andExpect(status().isBadRequest)
  }

  @Test
  fun `contact search returns a 503 when feature flag is disabled using a POST`() {
    whenever(featureFlagConfig.getConfigFlagValue(FeatureFlagConfig.ADDRESS_SEARCH_ENDPOINT_ENABLED)).thenReturn(false)
    postToApi(path, "")
      .andExpect(status().isServiceUnavailable)
  }
}
