package uk.gov.justice.digital.hmpps.hmppsintegrationapi.smoke.person

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIHttpClient
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class PersonSmokeTest : DescribeSpec(
  {
    val basePath = "v1/persons"
    val httpClient = IntegrationAPIHttpClient()
    val hmppsId = "2004/13116M"
    val encodedHmppsId = URLEncoder.encode(hmppsId, StandardCharsets.UTF_8)

    it("returns a list of persons using first name and last name as search parameters") {
      val firstName = "Example_First_Name"
      val lastName = "Example_Last_Name"
      val queryParams = "first_name=$firstName&last_name=$lastName"

      val response = httpClient.performAuthorised("$basePath?$queryParams")

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

    it("returns a person from Prisoner Offender Search and Probation Offender Search") {
      val response = httpClient.performAuthorised("$basePath/$encodedHmppsId")

      response.body().shouldBe(
        """
  {
    "data": {
      "firstName": "string",
      "lastName": "string",
      "middleName": "string",
      "dateOfBirth": "2019-08-24",
      "gender": "string",
      "ethnicity": "string",
      "aliases": [
        {
          "firstName": "string",
          "lastName": "string",
          "middleName": "string",
          "dateOfBirth": "2019-08-24",
          "gender": "string",
          "ethnicity": null
        }
      ],
      "identifiers": {
          "nomisNumber": "G5555TT",
          "croNumber": "123456/24A",
          "deliusCrn": "A123456"
      },
      "pncId": "2012/0052494Q",
      "hmppsId": "A123456",
      "contactDetails": {
        "phoneNumbers": [
          {
            "number": "string",
            "type": "TELEPHONE"
          }
        ],
        "emails":null
      }
    }
  }
    """.removeWhitespaceAndNewlines(),
      )
    }

    it("returns image metadata for a person") {
      val response = httpClient.performAuthorised("$basePath/$encodedHmppsId/images")

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
  },
)
