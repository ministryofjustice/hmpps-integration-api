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
    val pncId = "2004/13116M"
    val encodedPncId = URLEncoder.encode(pncId, StandardCharsets.UTF_8)

    val baseUrl = "http://localhost:8080"
    val basePath = "v1/persons/$encodedPncId/sentences"

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

      response.statusCode().shouldBe(HttpStatus.OK.value())
      response.body().shouldEqualJson(
        """
        {
          "data": [
            {
              "dateOfSentencing": "2019-08-24",
              "description": "string",
              "isActive": null,
              "isCustodial": true,
              "terms": [
                {
                  "years": 1,
                  "months": 2,
                  "weeks": 3,
                  "days": 4,
                  "hours": null
                }
              ]
            },
            {
              "dateOfSentencing": "2019-08-24",
              "description": "string",
              "isActive": true,
              "isCustodial": false,
              "terms": [
                {
                  "years": null,
                  "months": null,
                  "weeks": null,
                  "days": null,
                  "hours": $hourMinIntValue
                }
              ]
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
  },
)
