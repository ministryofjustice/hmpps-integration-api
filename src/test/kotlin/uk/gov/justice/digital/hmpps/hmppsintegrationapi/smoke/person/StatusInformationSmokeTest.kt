package uk.gov.justice.digital.hmpps.hmppsintegrationapi.smoke.person

import io.kotest.assertions.json.shouldEqualJson
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIHttpClient
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class StatusInformationSmokeTest : DescribeSpec(
  {
    val hmppsId = "2010/15229L"
    val encodedHmppsId = URLEncoder.encode(hmppsId, StandardCharsets.UTF_8)
    val basePath = "v1/persons/$encodedHmppsId/status-information"
    val httpClient = IntegrationAPIHttpClient()

    it("returns status information for a person") {
      val response = httpClient.performAuthorised(basePath)

      response.statusCode().shouldBe(HttpStatus.OK.value())
      response.body().shouldEqualJson(
        """
        {
          "data": [
            {
              "code": "ASFO",
              "description": "Serious Further Offence - Subject to SFO review/investigation",
              "startDate": "2019-08-24",
              "reviewDate": "2019-08-24",
              "notes": "string"
            }
        ],
        "pagination": {
          "isLastPage": true,
          "count": 1,
          "page": 1,
          "perPage": 10,
          "totalCount": 1,
          "totalPages": 1
        }
      }
      """.removeWhitespaceAndNewlines(),
      )
    }
  },
)
