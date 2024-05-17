package uk.gov.justice.digital.hmpps.hmppsintegrationapi.smoke.person

import io.kotest.assertions.json.shouldEqualJson
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIHttpClient
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class OffencesSmokeTest : DescribeSpec(
  {
    val hmppsId = "2004/13116M"
    val encodedHmppsId = URLEncoder.encode(hmppsId, StandardCharsets.UTF_8)
    val basePath = "v1/persons/$encodedHmppsId/offences"
    val httpClient = IntegrationAPIHttpClient()

    it("returns offences for a person") {
      val response = httpClient.performAuthorised(basePath)

      response.statusCode().shouldBe(HttpStatus.OK.value())
      response.body().shouldEqualJson(
        """
        {
          "data": [
            {
              "serviceSource": "NOMIS",
              "systemSource": "PRISON_SYSTEMS",
              "cjsCode": "RR84070",
              "hoCode": null,
              "courtDates": [
                "2018-02-10"
              ],
              "courtName": "string",
              "description": "Commit an act / series of acts with intent to pervert the course of public justice",
              "endDate": "2018-03-10",
              "startDate": "2018-02-10",
              "statuteCode": "RR84"
            },
            {
              "serviceSource": "NDELIUS",
              "systemSource": "PROBATION_SYSTEMS",
              "cjsCode": null,
              "hoCode": "string",
              "courtDates": [
                "2019-08-24"
              ],
              "courtName": "string",
              "description": "string",
              "endDate": null,
              "startDate": "2019-08-24",
              "statuteCode": null
            },
            {
              "serviceSource": "NDELIUS",
              "systemSource": "PROBATION_SYSTEMS",
              "cjsCode": null,
              "hoCode": "string",
              "courtDates": [
                "2019-08-24"
              ],
              "courtName": "string",
              "description": "string",
              "endDate": null,
              "startDate": "2019-08-24",
              "statuteCode": null
            }
         ],
        "pagination": {
          "isLastPage": true,
          "count": 3,
          "page": 1,
          "perPage": 10,
          "totalCount": 3,
          "totalPages": 1
        }
      }
      """.removeWhitespaceAndNewlines(),
      )
    }
  },
)
