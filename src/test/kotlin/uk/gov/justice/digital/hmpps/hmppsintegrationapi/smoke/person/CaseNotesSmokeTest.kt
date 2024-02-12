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
          "data":
            {
              "caseNotes": [
                {
                  "caseNoteId": "12311312"
                }
              ]
            }
         }

        """.removeWhitespaceAndNewlines(),
      )
    }
  },
)
