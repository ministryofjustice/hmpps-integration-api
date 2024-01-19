package uk.gov.justice.digital.hmpps.hmppsintegrationapi.smoke.person

import io.kotest.assertions.json.shouldEqualJson
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIHttpClient
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class AdjudicationsSmokeTest : DescribeSpec(
  {
    val hmppsId = "G2996UX"
    val encodedHmppsId = URLEncoder.encode(hmppsId, StandardCharsets.UTF_8)
    val basePath = "v1/persons/$encodedHmppsId/reported-adjudications"
    val httpClient = IntegrationAPIHttpClient()

    it("returns adjudications for a person") {
      val response = httpClient.performAuthorised(basePath)

      response.statusCode().shouldBe(HttpStatus.OK.value())
      response.body().shouldEqualJson(
        """
         {
          "data": [
              {
                "incidentDetails":
                  {
                    "locationId": 123,
                    "dateTimeOfIncident": "2021-07-05T10:35:17",
                    "dateTimeOfDiscovery": "2021-07-05T10:35:17",
                    "handoverDeadline": "2021-07-05T10:35:17"
                  },
                "isYouthOffender": true,
                "incidentRole": {
                    "roleCode": "25a",
                    "offenceRule": {
                      "paragraphNumber": "25(a)",
                      "paragraphDescription": "Committed an assault"
                    },
                    "associatedPrisonersNumber": "G2996UX",
                    "associatedPrisonersName": "G2996UX"
                  },
                  "offenceDetails": {
                    "offenceCode": 3,
                    "offenceRule": {
                      "paragraphNumber": "25(a)",
                      "paragraphDescription": "Committed an assault",
                      "nomisCode": "string",
                      "withOthersNomisCode": "string"
                    },
                    "victimPrisonersNumber": "G2996UX",
                    "victimStaffUsername": "ABC12D",
                    "victimOtherPersonsName": "Bob Hope"
                  },
                ],
          "pagination": {
            "isLastPage": true,
            "count": 1,
            "page": 1,
            "perPage": 8,
            "totalCount": 1,
            "totalPages": 1
          }
        }
        """.removeWhitespaceAndNewlines(),
      )
    }
  },
)
