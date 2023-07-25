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

      response.statusCode().shouldBe(HttpStatus.OK.value())
      response.body().shouldEqualJson(
        """
        {
          "data": [
            {
              "startDate": "2018-12-31",
              "length": {
                "days": 4,
                "weeks": 3,
                "months": 2,
                "years": 1
              },
              "fineAmount": -1.7976931348623157E308,
              "isLifeSentence": true
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
