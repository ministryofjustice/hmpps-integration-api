package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig

class ContactsIntegrationTest : IntegrationTestBase() {
  @Test
  fun `gets contact by contact id`() {
    callApi("/v1/contacts/$contactId")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("person-contacts.json"), JsonCompareMode.STRICT))
  }

  @Test
  fun `successfully searches contacts by firstName and lastName using a GET`() {
    callApi("/v1/contacts?firstName=John&lastName=Doe")
      .andExpect(status().isOk)
      .andExpect(header().string("Cache-Control", "no-cache"))
  }

  @Test
  fun `contact search returns a bad request when no search criteria using a GET`() {
    callApi("/v1/contacts")
      .andExpect(status().isBadRequest)
  }

  @Test
  fun `contact search returns a 503 when feature flag is disabled using a GET`() {
    whenever(featureFlagConfig.getConfigFlagValue(FeatureFlagConfig.CONTACT_SEARCH_ENDPOINT_ENABLED)).thenReturn(false)
    callApi(
      "/v1/contacts?firstName=John&lastName=Doe",
    ).andExpect(status().isServiceUnavailable)
  }

  @Test
  fun `successfully searches contacts by firstName and lastName using a POST`() {
    postToApi(
      "/v1/contacts",
      """
      {
        "firstName":"John",
        "lastName":"Doe"
      }
      """.trimIndent(),
    ).andExpect(status().isOk)
      .andExpect(header().string("Cache-Control", "no-cache"))
  }

  @Test
  fun `contact search returns a bad request when no search criteria using a POST`() {
    postToApi(
      "/v1/contacts",
      "",
    ).andExpect(status().isBadRequest)
  }

  @Test
  fun `contact search returns a 503 when feature flag is disabled using a POST`() {
    whenever(featureFlagConfig.getConfigFlagValue(FeatureFlagConfig.CONTACT_SEARCH_ENDPOINT_ENABLED)).thenReturn(false)
    postToApi(
      "/v1/contacts",
      "",
    ).andExpect(status().isServiceUnavailable)
  }

  @Test
  fun `GET linked prisoners returns a 200`() {
    callApi("/v1/contacts/$contactId/linked-prisoners")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("person-linked-prisoners.json"), JsonCompareMode.STRICT))
  }

  @Test
  fun `GET linked prisoners returns a 503 when feature flag is disabled `() {
    whenever(featureFlagConfig.getConfigFlagValue(FeatureFlagConfig.CONTACT_LINKED_PRISONERS_ENDPOINT_ENABLED)).thenReturn(false)
    callApi(
      "/v1/contacts/$contactId/linked-prisoners",
    ).andExpect(status().isServiceUnavailable)
  }
}
