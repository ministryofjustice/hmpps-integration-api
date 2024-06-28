package uk.gov.justice.digital.hmpps.hmppsintegrationapi.smoke

import io.kotest.assertions.json.shouldEqualJson
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIHttpClient

class PESPrisonerDetailSmokeTest : DescribeSpec(
  {
    val hmppsId = "X123456"
    val basePath = "v1/pes/prisoner-details/$hmppsId"

    val httpClient = IntegrationAPIHttpClient()

    it("returns a prisoner detail for a HmppsID") {
      val response = httpClient.performAuthorised(basePath)

      response.statusCode().shouldBe(HttpStatus.OK.value())
      response.body().shouldEqualJson(
        """
        {
        "data": {
          "prisonerNumber": "A1234AA",
          "firstName": "Robert",
          "lastName": "Larsen",
          "prisonId": "MDI",
          "prisonName": "HMP Leeds",
          "cellLocation": "A-1-002"
        },
        "errors": []
      }
      """.removeWhitespaceAndNewlines(),
      )
    }
  },
)
