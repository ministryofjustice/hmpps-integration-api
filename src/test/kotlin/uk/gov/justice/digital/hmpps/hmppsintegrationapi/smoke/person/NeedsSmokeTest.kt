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

class NeedsSmokeTest : DescribeSpec(
  {
    val hmppsId = "2004/13116M"
    val encodedHmppsId = URLEncoder.encode(hmppsId, StandardCharsets.UTF_8)

    val baseUrl = "http://localhost:8080"
    val basePath = "v1/persons/$encodedHmppsId/needs"

    val httpClient = HttpClient.newBuilder().build()
    val httpRequest = HttpRequest.newBuilder()

    it("returns needs for a person") {
      val response = httpClient.send(
        httpRequest.uri(URI.create("$baseUrl/$basePath")).build(),
        HttpResponse.BodyHandlers.ofString(),
      )

      response.statusCode().shouldBe(HttpStatus.OK.value())
      response.body().shouldEqualJson(
        """
        {
          "data": {
            "assessedOn": "2023-10-04T07:06:43",
            "identifiedNeeds": [
              {
                "type": "DRUG_MISUSE"
              }
            ],
            "notIdentifiedNeeds": [
              {
                "type": "DRUG_MISUSE"
              }
            ],
            "unansweredNeeds": [
              {
                "type": "DRUG_MISUSE"
              }
            ]
          }
        }
        """.removeWhitespaceAndNewlines(),
      )
    }
  },
)
