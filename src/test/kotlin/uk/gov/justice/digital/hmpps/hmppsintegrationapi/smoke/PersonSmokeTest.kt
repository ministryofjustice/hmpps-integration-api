package uk.gov.justice.digital.hmpps.hmppsintegrationapi.smoke

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class PersonSmokeTest : DescribeSpec({
  val baseUrl = "http://localhost:8080"
  val httpClient = HttpClient.newBuilder().build()
  val httpRequest = HttpRequest.newBuilder()
  val id = "A1234AL"

  it("returns a person from NOMIS, Prisoner Offender Search and Probation Offender Search") {
    val response = httpClient.send(
      httpRequest.uri(URI.create("$baseUrl/persons/$id")).build(),
      HttpResponse.BodyHandlers.ofString()
    )

    response.body().shouldContain("\"nomis\":{\"firstName\":\"string\",\"lastName\":\"string\"")
  }

  it("returns image metadata for a person") {
    val response = httpClient.send(
      httpRequest.uri(URI.create("$baseUrl/persons/$id/images")).build(),
      HttpResponse.BodyHandlers.ofString()
    )

    response.body().shouldBe(
      """
      {
        "images": [
          {
            "id": 2461788,
            "captureDate": "2008-08-27",
            "view": "FACE",
            "orientation": "FRONT",
            "type": "OFF_BKG"
          }
        ]
      }
      """.removeWhitespaceAndNewlines()
    )
  }

  it("returns addresses for a person") {
    val response = httpClient.send(
      httpRequest.uri(URI.create("$baseUrl/persons/$id/addresses")).build(),
      HttpResponse.BodyHandlers.ofString()
    )

    response.statusCode().shouldBe(HttpStatus.OK.value())
    response.body().shouldBe(
      """
      {
        "addresses": [
          {
            "postcode": "string"
          },
          {
            "postcode": "LI1 5TH"
          }
        ]
      }
      """.removeWhitespaceAndNewlines()
    )
  }
})
