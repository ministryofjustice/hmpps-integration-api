package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig

class ContactsIntegrationTest : IntegrationTestBase() {
  @Test
  fun `gets contact by contact id`() {
    callApi("/v1/contacts/$contactId")
      .andExpect(status().isOk)
      .andExpect(
        content().json(
          """
          {
            "data": {
              "contactId": 123456,
              "titleCode": "MR",
              "titleDescription": "Mr",
              "lastName": "Doe",
              "firstName": "John",
              "middleNames": "William",
              "dateOfBirth": "1980-01-01",
              "isStaff": false,
              "deceasedDate": "1980-01-01",
              "languageCode": "ENG",
              "languageDescription": "English",
              "interpreterRequired": true,
              "addresses": [
                {
                  "addressType": "HOME",
                  "addressTypeDescription": "HOME",
                  "primaryAddress": true,
                  "flat": "Flat 2B",
                  "property": "Mansion House",
                  "street": "Acacia Avenue",
                  "area": "Morton Heights",
                  "cityCode": "25343",
                  "cityDescription": "Sheffield",
                  "countyCode": "S.YORKSHIRE",
                  "countyDescription": "South Yorkshire",
                  "postcode": "S13 4FH",
                  "countryCode": "ENG",
                  "countryDescription": "England",
                  "verified": false,
                  "verifiedBy": "NJKG44D",
                  "verifiedTime": "2024-01-01T00:00:00Z",
                  "mailFlag": false,
                  "startDate": "2024-01-01",
                  "endDate": "2024-01-01",
                  "noFixedAddress": false,
                  "comments": "Some additional information",
                  "phoneNumbers": [
                    {
                      "phoneType": "MOB",
                      "phoneTypeDescription": "Mobile phone",
                      "phoneNumber": "+1234567890",
                      "extNumber": "123"
                    }
                  ],
                  "createdTime": "2024-01-01T00:00:00Z",
                  "updatedTime": "2024-01-01T00:00:00Z"
                }
              ],
              "phoneNumbers": [
                {
                  "phoneType": "MOB",
                  "phoneTypeDescription": "Mobile",
                  "phoneNumber": "+1234567890",
                  "extNumber": "123"
                }
              ],
              "emailAddresses": [
                {
                  "emailAddress": "test@example.com"
                }
              ],
              "genderCode": "M",
              "genderDescription": "Male"
            },
            "errors": []
          }
          """.trimIndent(),
        ),
      )
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
}
