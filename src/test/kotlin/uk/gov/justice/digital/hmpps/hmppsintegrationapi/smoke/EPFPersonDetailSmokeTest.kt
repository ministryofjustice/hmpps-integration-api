package uk.gov.justice.digital.hmpps.hmppsintegrationapi.smoke

import io.kotest.assertions.json.shouldEqualJson
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIHttpClient

class EPFPersonDetailSmokeTest : DescribeSpec(
  {
    val hmppsId = "X123456"
    val eventNumber = 1234
    val basePath = "v1/epf/person-details/$hmppsId/$eventNumber"

    val httpClient = IntegrationAPIHttpClient()

    it("returns a person detail for a probation case, by HmppsID") {
      val response = httpClient.performAuthorised(basePath)

      response.statusCode().shouldBe(HttpStatus.OK.value())
      response.body().shouldEqualJson(
        """
      {
        "data": {
          "nomsId": "string",
          "name": {
            "forename": "string",
            "middleName": "string",
            "surname": "string"
          },
          "dateOfBirth": "2019-08-24",
          "gender": "string",
          "sentence": {
            "date": null,
            "sentencingCourt": {
              "name": null
            },
            "releaseDate": null
          },
          "responsibleProvider": {
            "code": "string",
            "name": "string"
          },
          "ogrsScore": -9007199254740991,
          "age": -9007199254740991,
          "ageAtRelease": -9007199254740991
        },
        "errors": []
      }
      """.removeWhitespaceAndNewlines(),
      )
    }
  },
)
