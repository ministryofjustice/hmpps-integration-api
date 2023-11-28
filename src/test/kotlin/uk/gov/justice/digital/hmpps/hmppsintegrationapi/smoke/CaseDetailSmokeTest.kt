package uk.gov.justice.digital.hmpps.hmppsintegrationapi.smoke

import io.kotest.assertions.json.shouldEqualJson
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class CaseDetailSmokeTest : DescribeSpec(
  {
    val hmppsId = "X123456"
    val eventNumber = 1234
    val baseUrl = "http://localhost:8080"
    val basePath = "v1/case-details/$hmppsId/$eventNumber"

    val httpClient = HttpClient.newBuilder().build()
    val httpRequest = HttpRequest.newBuilder()

    it("returns a case detail for a probation case, by HmppsID") {
      val response = httpClient.send(
        httpRequest.uri(URI.create("$baseUrl/$basePath")).build(),
        HttpResponse.BodyHandlers.ofString(),
      )

      response.statusCode().shouldBe(HttpStatus.OK.value())
      response.body().shouldEqualJson(
        """
      {
        "nomsId": "string",
        "name": {
          "forename": "string",
          "middleName": "string",
          "surname": "string"
        },
        "dateOfBirth": "string",
        "gender": "string",
        "sentence": {
          "date": "string",
          "sentencingCourt": {
            "name": "string"
          },
          "releaseDate": "string"
        },
        "responsibleProvider": {
          "code": "string",
          "name": "string"
        },
        "ogrsScore": "integer",
        "age": "integer",
        "ageAtRelease": "integer"
      }
      """.removeWhitespaceAndNewlines(),
      )
    }
  },
)
