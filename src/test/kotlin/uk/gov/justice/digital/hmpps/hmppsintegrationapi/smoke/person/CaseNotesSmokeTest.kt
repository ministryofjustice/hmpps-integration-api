package uk.gov.justice.digital.hmpps.hmppsintegrationapi.smoke.person

import io.kotest.assertions.json.shouldEqualJson
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIHttpClient
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class CaseNotesSmokeTest : DescribeSpec(
  {
    val hmppsId = "ABC123"
    val encodedHmppsId = URLEncoder.encode(hmppsId, StandardCharsets.UTF_8)
    val basePath = "v1/persons/$encodedHmppsId/case-notes"
    val httpClient = IntegrationAPIHttpClient()

    it("returns case notes for a person.") {
      val response = httpClient.performAuthorised(basePath)
      response.statusCode().shouldBe(HttpStatus.OK.value())
      response.body().shouldEqualJson(
        """
         {
          "data": [
          {
             "caseNoteId": "12311312",
            "offenderIdentifier": "A1234AA",
            "type": "KA",
            "typeDescription": "Key Worker",
            "subType": "KS",
            "subTypeDescription": "Key Worker Session",
            "creationDateTime": "2019-08-24T14:15:22",
            "occurrenceDateTime": "2019-08-24T14:15:22",
            "text": "This is some text",
            "locationId": "MDI",
            "sensitive": true,
            "amendments": [
              {
                "caseNoteAmendmentId": 123232,
                "creationDateTime": "2019-08-24T14:15:22",
                "additionalNoteText": "Some Additional Text"
              }
            ]
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
