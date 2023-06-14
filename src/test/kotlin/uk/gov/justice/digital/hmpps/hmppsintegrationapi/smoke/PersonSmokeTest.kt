package uk.gov.justice.digital.hmpps.hmppsintegrationapi.smoke

import io.kotest.assertions.json.shouldEqualJson
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

class PersonSmokeTest : DescribeSpec(
  {
    val baseUrl = "http://localhost:8080"
    val basePath = "v1/persons"
    val httpClient = HttpClient.newBuilder().build()
    val httpRequest = HttpRequest.newBuilder()

    val pncId = "2004/13116M"
    val encodedPncId = URLEncoder.encode(pncId, StandardCharsets.UTF_8)

    it("returns a list of persons using first name and last name as search parameters") {
      val firstName = "Example_First_Name"
      val lastName = "Example_Last_Name"
      val queryParams = "first_name=$firstName&last_name=$lastName"

      val response = httpClient.send(
        httpRequest.uri(URI.create("$baseUrl/$basePath?$queryParams")).build(),
        HttpResponse.BodyHandlers.ofString(),
      )

      response.statusCode().shouldBe(HttpStatus.OK.value())
      response.body().shouldContain("\"data\":[")
      response.body().shouldContain(
        """
          "firstName":"Robert",
          "lastName":"Larsen"
        """.removeWhitespaceAndNewlines(),
      )
      response.body().shouldContain(
        """
          "firstName":"string",
          "lastName":"string"
        """.removeWhitespaceAndNewlines(),
      )
    }

    it("returns a person from NOMIS, Prisoner Offender Search and Probation Offender Search") {
      val response = httpClient.send(
        httpRequest.uri(URI.create("$baseUrl/$basePath/$encodedPncId")).build(),
        HttpResponse.BodyHandlers.ofString(),
      )

      response.body().shouldBe(
        """
          {
            "prisonerOffenderSearch": {
              "firstName": "Robert",
              "lastName": "Larsen",
              "middleName": "John James",
              "dateOfBirth": "1975-04-02",
              "aliases": [
                {
                  "firstName": "Robert",
                  "lastName": "Lorsen",
                  "middleName": "Trevor",
                  "dateOfBirth": "1975-04-02"
                }
              ],
              "prisonerId": "A1234AA",
              "pncId": "12/394773H"
            },
            "probationOffenderSearch": {
              "firstName": "string",
              "lastName": "string",
              "middleName": "string",
              "dateOfBirth": "2019-08-24",
              "aliases": [
                {
                  "firstName": "string",
                  "lastName": "string",
                  "middleName": "string",
                  "dateOfBirth": "2019-08-24"
                }
              ],
              "prisonerId": null,
              "pncId": "string"
            }
          }
        """.removeWhitespaceAndNewlines(),
      )
    }

    it("returns image metadata for a person") {
      val response = httpClient.send(
        httpRequest.uri(URI.create("$baseUrl/$basePath/$encodedPncId/images")).build(),
        HttpResponse.BodyHandlers.ofString(),
      )

      response.statusCode().shouldBe(HttpStatus.OK.value())
      response.body().shouldContain("\"data\":[")
      response.body().shouldContain(
        """
            "id":2461788,
            "active":false,
            "captureDateTime":"2021-07-05T10:35:17",
            "view":"OIC",
            "orientation":"NECK",
            "type":"OFF_IDM"
      """.removeWhitespaceAndNewlines(),
      )
    }

    it("returns addresses for a person") {
      val response = httpClient.send(
        httpRequest.uri(URI.create("$baseUrl/$basePath/$encodedPncId/addresses")).build(),
        HttpResponse.BodyHandlers.ofString(),
      )

      response.statusCode().shouldBe(HttpStatus.OK.value())
      response.body().shouldEqualJson(
        """
      {
        "data": [
          {
            "country": "ENG",
            "county": "HEREFORD",
            "endDate": "2021-02-12",
            "locality": "Brincliffe",
            "name": "Liverpool Prison",
            "noFixedAddress": false,
            "number": "3B",
            "postcode": "LI1 5TH",
            "startDate": "2005-05-12",
            "street": "Slinn Street",
            "town": "Liverpool",
            "types": [
              {
                "code": "HDC",
                "description": "HDC Address"
              },
              {
                "code": "BUS",
                "description": "Business Address"
              }
            ],
            "notes": null
          },
          {
            "country": null,
            "county": "string",
            "endDate": "2019-08-24",
            "locality": "string",
            "name": "string",
            "noFixedAddress": true,
            "number": "string",
            "postcode": "string",
            "startDate": "2019-08-24",
            "street": "string",
            "town": "string",
            "types": [
              {
                "code": "string",
                "description": "string"
              }
            ],
            "notes": "string"
          }
        ]
      }
      """.removeWhitespaceAndNewlines(),
      )
    }
  },
)
