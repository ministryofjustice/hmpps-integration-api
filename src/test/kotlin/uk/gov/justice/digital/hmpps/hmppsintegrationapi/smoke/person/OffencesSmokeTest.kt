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

class OffencesSmokeTest : DescribeSpec(
  {
    val pncId = "2004/13116M"
    val encodedPncId = URLEncoder.encode(pncId, StandardCharsets.UTF_8)

    val baseUrl = "http://localhost:8080"
    val basePath = "v1/persons/$encodedPncId/offences"

    val httpClient = HttpClient.newBuilder().build()
    val httpRequest = HttpRequest.newBuilder()

    xit("returns offences for a person") {
      val response = httpClient.send(
        httpRequest.uri(URI.create("$baseUrl/$basePath")).build(),
        HttpResponse.BodyHandlers.ofString(),
      )

      response.statusCode().shouldBe(HttpStatus.OK.value())
      response.body().shouldEqualJson(
        """
        {
          "data": [
          {
            "cjsCode": "RR84070",
            "courtDate": "2018-02-10",
            "description": "Commit an act / series of acts with intent to pervert the course of public justice",
            "endDate": "2018-03-10",
            "startDate": "2018-02-10",
            "statuteCode": "RR84"
          },
          {
            "cjsCode": null,
            "courtDate": null,
            "description": "Commit an act / series of acts with intent to pervert the course of public justice",
            "endDate": null,
            "startDate": null,
            "statuteCode": null
          }
        ],
          "pagination": {
            "isLastPage": true,
            "count": 1,
            "page": 1,
            "perPage": 10,
            "totalCount": 1,
            "totalPages": 1
          }
        }
        """.removeWhitespaceAndNewlines(),
      )
    }
  },
)
