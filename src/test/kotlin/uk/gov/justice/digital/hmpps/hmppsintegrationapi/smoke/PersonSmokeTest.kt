package uk.gov.justice.digital.hmpps.hmppsintegrationapi.smoke

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import wiremock.org.eclipse.jetty.http.HttpStatus
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class PersonSmokeTest : DescribeSpec({
  val baseUrl = "http://localhost:8080"
  val httpClient = HttpClient.newBuilder().build()
  val httpRequest = HttpRequest.newBuilder()

  it("returns a list of persons using first name and last name as search parameters") {
    val firstName = "Example_First_Name"
    val lastName = "Example_Last_Name"
    val queryParams = "firstName=$firstName&lastName=$lastName"

    val response = httpClient.send(
      httpRequest.uri(URI.create("$baseUrl/persons?$queryParams")).build(),
      HttpResponse.BodyHandlers.ofString()
    )

    response.statusCode().shouldBe(HttpStatus.OK_200)
    response.body().shouldContain("\"persons\":[")
    response.body().shouldContain(
      """
        "firstName":"Robert",
        "lastName":"Larsen"
      """.removeWhitespaceAndNewlines()
    )
    response.body().shouldContain(
      """
        "firstName":"string",
        "lastName":"string"
      """.removeWhitespaceAndNewlines()
      )
  }

  it("returns a person from NOMIS, Prisoner Offender Search and Probation Offender Search when given an id") {
    val id = "A1234AL"

    val response = httpClient.send(
      httpRequest.uri(URI.create("$baseUrl/persons/$id")).build(),
      HttpResponse.BodyHandlers.ofString()
    )

    response.body().shouldContain("\"nomis\":{\"firstName\":\"string\",\"lastName\":\"string\"")
  }

  it("returns image metadata for a person") {
    val id = "A1234AL"

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
})
