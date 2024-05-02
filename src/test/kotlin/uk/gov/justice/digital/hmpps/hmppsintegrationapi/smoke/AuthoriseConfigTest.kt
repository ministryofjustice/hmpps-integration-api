package uk.gov.justice.digital.hmpps.hmppsintegrationapi.smoke

import io.kotest.assertions.json.shouldEqualJson
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIHttpClient

class AuthoriseConfigTest : DescribeSpec(
  {
    val httpClient = IntegrationAPIHttpClient()

    it("returns authorise config") {

      val response = httpClient.performAuthorised("v1/config/authorisation", "config-test")

      response.statusCode().shouldBe(HttpStatus.OK.value())
      response.body().shouldEqualJson(
        """
      {
        "automated-test-client": [
          "/v1/persons",
          "/v1/persons/\\.*+[^/]*${'$'}",
          "/v1/persons/.*/images",
          "/v1/images/\\.*+[^/]*${'$'}",
          "/v1/persons/.*/addresses",
          "/v1/persons/.*/offences",
          "/v1/persons/.*/alerts",
          "/v1/persons/.*/sentences",
          "/v1/persons/.*/sentences/latest-key-dates-and-adjustments",
          "/v1/persons/.*/risks/scores",
          "/v1/persons/.*/needs",
          "/v1/persons/.*/risks/serious-harm",
          "/v1/persons/.*/reported-adjudications",
          "/v1/persons/.*/adjudications",
          "/v1/persons/.*/licences/conditions",
          "/v1/persons/.*/protected-characteristics",
          "/v1/persons/.*/risks/mappadetail",
          "/v1/persons/.*/risks/categories",
          "/v1/persons/.*/case-notes",
          "/v1/persons/.*/person-responsible-officer",
          "/v1/epf/person-details/.*/\\.*+[^/]*${'$'}",
          "/health",
          "/health/ping",
          "/health/readiness",
          "/health/liveness",
          "/info"
        ],
        "config-test": [
          "/v1/config/authorisation"
        ]
      }
      """.removeWhitespaceAndNewlines(),
      )
    }
  },
)
