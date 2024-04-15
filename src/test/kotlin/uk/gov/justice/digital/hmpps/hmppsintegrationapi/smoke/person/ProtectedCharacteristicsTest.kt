package uk.gov.justice.digital.hmpps.hmppsintegrationapi.smoke.person

import io.kotest.assertions.json.shouldEqualJson
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIHttpClient
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class ProtectedCharacteristicsTest : DescribeSpec(
  {
    val hmppsId = "2004/13116M"
    val encodedHmppsId = URLEncoder.encode(hmppsId, StandardCharsets.UTF_8)
    val basePath = "v1/persons/$encodedHmppsId/protected-characteristics"
    val httpClient = IntegrationAPIHttpClient()

    it("returns protected characteristics for a person") {
      val response = httpClient.performAuthorised(basePath)

      response.statusCode().shouldBe(HttpStatus.OK.value())
      response.body().shouldEqualJson(
        """
        {
          "data": {
            "age": -2147483648,
            "gender": "string",
            "sexualOrientation": "string",
            "ethnicity": "string",
            "nationality": "string",
            "religion": "string",
            "disabilities": [
                {
                    "disabilityType": {
                        "code": "string",
                        "description": "string"
                    },
                    "condition": {
                        "code": "string",
                        "description": "string"
                    },
                    "startDate": "2019-08-24",
                    "endDate": "2019-08-24",
                    "notes": "string"
                }
            ],
            "maritalStatus": "Widowed",
            "reasonableAdjustments": [
                {
                    "treatmentCode": "WHEELCHR_ACC",
                    "commentText": "abcd",
                    "startDate": "2010-06-21",
                    "endDate": "2010-06-21",
                    "treatmentDescription": "Wheelchair accessibility"
                }
            ]
        }
      }
      """.removeWhitespaceAndNewlines(),
      )
    }
  },
)
