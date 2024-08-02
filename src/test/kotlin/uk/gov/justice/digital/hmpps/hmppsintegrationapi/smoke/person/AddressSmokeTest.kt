package uk.gov.justice.digital.hmpps.hmppsintegrationapi.smoke.person

import io.kotest.assertions.json.shouldEqualJson
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIHttpClient
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class AddressSmokeTest : DescribeSpec(
  {
    val hmppsId = "2003/13116M"
    val encodedHmppsId = URLEncoder.encode(hmppsId, StandardCharsets.UTF_8)
    val basePath = "v1/persons"
    val httpClient = IntegrationAPIHttpClient()

    it("returns addresses for a person") {
      val response = httpClient.performAuthorised("$basePath/$encodedHmppsId/addresses")

      response.body().shouldEqualJson(
        """
      {
        "data": [
          {
            "country": "ENG",
            "county": "HEREFORD",
            "endDate": "2021-02-12",
            "locality": "Brincliffe",
            "name": "Liverpool Prison",
            "noFixedAddress": false,
            "number": "3B",
            "postcode": "LI1 5TH",
            "startDate": "2005-05-12",
            "street": "Slinn Street",
            "town": "Liverpool",
            "types": [
              {
                "code": "HDC",
                "description": "HDC Address"
              },
              {
                "code": "BUS",
                "description": "Business Address"
              }
            ],
            "notes": "This is a comment text"
          },
          {
            "country": null,
            "county": "string",
            "endDate": "2019-08-24",
            "locality": "string",
            "name": "string",
            "noFixedAddress": true,
            "number": "string",
            "postcode": "string",
            "startDate": "2019-08-24",
            "street": "string",
            "town": "string",
            "types": [
              {
                "code": "string",
                "description": "string"
              }
            ],
            "notes": "string"
          }
        ]
      }
      """.removeWhitespaceAndNewlines(),
      )
      response.statusCode().shouldBe(HttpStatus.OK.value())
    }
  },
)
