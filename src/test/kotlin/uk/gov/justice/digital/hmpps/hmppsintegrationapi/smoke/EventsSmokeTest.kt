package uk.gov.justice.digital.hmpps.hmppsintegrationapi.smoke

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

class EventsSmokeTest : DescribeSpec(
  {
    val deliusCrn = "X00001"
    val encodedDeliusCrn = URLEncoder.encode(deliusCrn, StandardCharsets.UTF_8)

    val baseUrl = "http://localhost:8080"
    val basePath = "v1/case/$encodedDeliusCrn/supervisions"

    val httpClient = HttpClient.newBuilder().build()
    val httpRequest = HttpRequest.newBuilder()

    it("returns a list of supervisions (called “events” in Delius) for a probation case, by CRN") {
      val response = httpClient.send(
        httpRequest.uri(URI.create("$baseUrl/$basePath")).build(),
        HttpResponse.BodyHandlers.ofString(),
      )

      response.statusCode().shouldBe(HttpStatus.OK.value())
      response.body().shouldEqualJson(
        """
        {
          "supervisions": [
            {
              "number": "integer",
              "active": "boolean",
              "date": "string",
              "sentence": {
                "description": "string",
                "date": "string",
                "length": "integer",
                "lengthUnits": "string",
                "custodial": "boolean"
              },
              "mainOffence": {
                "date": "string",
                "count": "integer",
                "code": "string",
                "description": "string",
                "mainCategory": {
                  "code": "string",
                  "description": "string"
                },
                "subCategory": {
                  "code": "string",
                  "description": "string"
                },
                "schedule15SexualOffence": "boolean",
                "schedule15ViolentOffence": "boolean"
              },
              "additionalOffences": [
                {
                  "date": "string",
                  "count": "integer",
                  "code": "string",
                  "description": "string",
                  "mainCategory": {
                    "code": "string",
                    "description": "string"
                  },
                  "subCategory": {
                    "code": "string",
                    "description": "string"
                  },
                  "schedule15SexualOffence": "boolean",
                  "schedule15ViolentOffence": "boolean"
                }
              ],
              "courtAppearances": [
                {
                  "type": "string",
                  "date": "string",
                  "court": "string",
                  "plea": "string"
                }
              ]
            }
          ]
        }
      """.removeWhitespaceAndNewlines(),
      )
    }
  },
)
