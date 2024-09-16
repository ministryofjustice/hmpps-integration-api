package uk.gov.justice.digital.hmpps.hmppsintegrationapi.smoke.person

import io.kotest.assertions.json.shouldEqualJson
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIHttpClient
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class NeedsSmokeTest : DescribeSpec(
  {
    val hmppsId = "2004/13116M"
    val encodedHmppsId = URLEncoder.encode(hmppsId, StandardCharsets.UTF_8)

    val basePath = "v1/persons/$encodedHmppsId/needs"

    val httpClient = IntegrationAPIHttpClient()

    it("returns needs for a person") {
      val response = httpClient.performAuthorised(basePath)

      response.statusCode().shouldBe(HttpStatus.OK.value())
      response.body().shouldEqualJson(
        """
        {
          "data": {
            "assessedOn": "2023-10-04T07:06:43",
            "identifiedNeeds": [
              {
                "type": "DRUG_MISUSE",
                "riskOfHarm": false,
                "riskOfReoffending": false,
                "severity": "NO_NEED"
              }
            ],
            "notIdentifiedNeeds": [
              {
                "type": "DRUG_MISUSE",
                "riskOfHarm": false,
                "riskOfReoffending": false,
                "severity": "NO_NEED"
              }
            ],
            "unansweredNeeds": [
              {
                "type": "DRUG_MISUSE",
                "riskOfHarm": false,
                "riskOfReoffending": false,
                "severity": "NO_NEED"
              }
            ]
          }
        }
        """.removeWhitespaceAndNewlines(),
      )
    }
  },
)
