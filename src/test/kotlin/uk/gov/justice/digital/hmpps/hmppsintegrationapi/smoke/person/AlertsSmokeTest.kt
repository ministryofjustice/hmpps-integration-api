package uk.gov.justice.digital.hmpps.hmppsintegrationapi.smoke.person

import io.kotest.assertions.json.shouldEqualJson
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIHttpClient
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class AlertsSmokeTest : DescribeSpec(
  {
    val hmppsId = "2004/13116M"
    val encodedHmppsId = URLEncoder.encode(hmppsId, StandardCharsets.UTF_8)

    val basePath = "v1/persons/$encodedHmppsId/alerts"
    val httpClient = IntegrationAPIHttpClient()

    it("returns alerts for a person") {
      val response = httpClient.performAuthorised(basePath)

      response.statusCode().shouldBe(HttpStatus.OK.value())
      response.body().shouldEqualJson(
        """
        {
          "data": [
            {
              "offenderNo": "G3878UK",
              "type": "X",
              "typeDescription": "Security",
              "code": "XER",
              "codeDescription": "Escape Risk",
              "comment": "Profession lock pick.",
              "dateCreated": "2019-08-20",
              "dateExpired": "2020-08-20",
              "expired": true,
              "active": false
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
