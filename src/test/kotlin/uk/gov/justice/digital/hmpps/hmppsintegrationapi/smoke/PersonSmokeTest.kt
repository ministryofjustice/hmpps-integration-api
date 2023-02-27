package uk.gov.justice.digital.hmpps.hmppsintegrationapi.smoke

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class PersonSmokeTest : DescribeSpec({
  val baseUrl = "http://localhost:8080"
  val httpClient = HttpClient.newBuilder().build()
  val httpRequest = HttpRequest.newBuilder()

  it("returns a list of persons using first name and last name as search parameters") {
    val firstName = "John"
    val lastName = "Wayne"

    val response = httpClient.send(
      httpRequest.uri(URI.create("$baseUrl/persons/foobar?firstName=$firstName&lastName=$lastName")).build(),
      HttpResponse.BodyHandlers.ofString()
    )

    response.body().shouldContain(
      """
      {
         "persons":[
            {
               "firstName":"John"
               "lastName":"Wayne"
               "middleName": "Guy"
            },
            {
               "firstName":"John"
               "lastName":"Wayne"
               "middleName": "Friend"
            },
         ]
      }
      """.trimIndent()
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
