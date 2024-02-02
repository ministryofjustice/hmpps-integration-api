package uk.gov.justice.digital.hmpps.hmppsintegrationapi.smoke.person

import io.kotest.assertions.json.shouldEqualJson
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIHttpClient
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class LicenceConditionSmokeTest : DescribeSpec(
  {
    val hmppsId = "G2996UX"
    val encodedHmppsId = URLEncoder.encode(hmppsId, StandardCharsets.UTF_8)
    val basePath = "v1/persons/$encodedHmppsId/licences/conditions"
    val httpClient = IntegrationAPIHttpClient()

    it("returns licence condition for a person") {
      val response = httpClient.performAuthorised(basePath)

      response.statusCode().shouldBe(HttpStatus.OK.value())
      response.body().shouldEqualJson(
        """
         {
          "data": {
            "hmppsId": "G2996UX",
            "offenderNumber": "A1234AA",
            "licences": [
              {
                "status": "IN_PROGRESS",
                "typeCode": "AP",
                "createdDate": "2023-11-20T00:00:00Z",
                "approvedDate": "2023-11-20T00:00:00Z",
                "updatedDate": "2023-11-20T00:00:00Z",
                "conditions": [
                  {
                    "type": "Bespoke",
                    "code": null,
                    "category": null,
                    "condition": "You should not visit Y"
                  },
                  {
                    "type": "Standard",
                    "code": "5a105297-dce1-4d18-b9ea-4195b46b7594",
                    "category": null,
                    "condition": "Not commit any offence."
                  },
                  {
                    "type": "STANDARD",
                    "code": "5a105297-dce1-4d18-b9ea-4195b46b7594",
                    "category": "Residence at a specific place",
                    "condition": "You must not enter the location X"
                  },
                  {
                    "type": "STANDARD",
                    "code": "5a105297-dce1-4d18-b9ea-4195b46b7594",
                    "category": "Residence at a specific place",
                    "condition": "You must not enter the location X"
                  },
                  {
                    "type": "Standard",
                    "code": "5a105297-dce1-4d18-b9ea-4195b46b7594",
                    "category": null,
                    "condition": "Not commit any offence."
                  }
                ]
              }
            ]
          }
        }
        """.removeWhitespaceAndNewlines(),
      )
    }
  },
)
