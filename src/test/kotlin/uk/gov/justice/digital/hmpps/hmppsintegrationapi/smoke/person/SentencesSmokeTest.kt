package uk.gov.justice.digital.hmpps.hmppsintegrationapi.smoke.person

import io.kotest.assertions.json.shouldEqualJson
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

class SentencesSmokeTest : DescribeSpec(
  {
    val hmppsId = "2004/13116M"
    val encodedHmppsId = URLEncoder.encode(hmppsId, StandardCharsets.UTF_8)

    val baseUrl = "http://localhost:8080"
    val basePath = "v1/persons/$encodedHmppsId/sentences"

    val httpClient = HttpClient.newBuilder().build()
    val httpRequest = HttpRequest.newBuilder()

    it("returns sentences for a person") {
      val response = httpClient.send(
        httpRequest.uri(URI.create("$baseUrl/$basePath")).build(),
        HttpResponse.BodyHandlers.ofString(),
      )

      /*No example data for an hour is provided in the OpenAPI specification for the supervisions endpoint
      Prism by default returns the minimum value of a Java/Kotlin Int ~ AP/18/08/2023*/
      val hourMinIntValue = -2147483648
      val fineAmountMinNumberValue = -1.7976931348623157E308

      response.statusCode().shouldBe(HttpStatus.OK.value())
      response.body().shouldEqualJson(
        """
        {
          "data": [
            {
              "dataSource": "NOMIS",
              "dateOfSentencing": "2019-08-24",
              "description": "string",
              "fineAmount": $fineAmountMinNumberValue,
              "isActive": null,
              "isCustodial": true,
              "length": {
                "duration": null,
                "units": null,
                "terms": [
                  {
                    "years": 1,
                    "months": 2,
                    "weeks": 3,
                    "days": 4,
                    "hours": null,
                    "prisonTermCode": "string"
                  }
                ]
              }
            },
            {
              "dataSource": "NDELIUS",
              "dateOfSentencing": "2019-08-24",
              "description": "string",
              "fineAmount": null,
              "isActive": true,
              "isCustodial": false,
              "length": {
                "duration": $hourMinIntValue,
                "units": "Hours",
                "terms": []
              }
            }
          ],
          "pagination": {
            "isLastPage": true,
            "count": 2,
            "page": 1,
            "perPage": 10,
            "totalCount": 2,
            "totalPages": 1
          }
        }
        """.removeWhitespaceAndNewlines(),
      )
    }

    it("returns latest sentence key dates and adjustments for a person") {
      val response = httpClient.send(
        httpRequest.uri(URI.create("$baseUrl/$basePath/latest-key-dates-and-adjustments")).build(),
        HttpResponse.BodyHandlers.ofString(),
      )

      response.statusCode().shouldBe(HttpStatus.OK.value())
      response.body().shouldEqualJson(
        """
        {
          "data": {
            "adjustments": {
              "additionalDaysAwarded": 12,
              "unlawfullyAtLarge": 12,
              "lawfullyAtLarge": 12,
              "restoredAdditionalDaysAwarded": 12,
              "specialRemission": 12,
              "recallSentenceRemand": 12,
              "recallSentenceTaggedBail": 12,
              "remand": 12,
              "taggedBail": 12,
              "unusedRemand": 12
            },
            "automaticRelease": {
              "date": "2020-02-03",
              "overrideDate: "2020-02-03",
              "calculatedDate": null
            },
            "conditionalRelease": {
              "date": "2020-02-03",
              "overrideDate: "2020-02-03",
              "calculatedDate": null
            },
            "dtoPostRecallRelease": {
              "date": "2020-02-03",
              "overrideDate: "2020-02-03",
              "calculatedDate": null
            }
          }
        }
        """.removeWhitespaceAndNewlines(),
      )
    }
  },
)
