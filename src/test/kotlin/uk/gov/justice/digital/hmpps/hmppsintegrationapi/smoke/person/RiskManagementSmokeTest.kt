package uk.gov.justice.digital.hmpps.hmppsintegrationapi.smoke.person

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIHttpClient
import java.net.URLEncoder
import java.net.http.HttpClient
import java.nio.charset.StandardCharsets

class RiskManagementSmokeTest : DescribeSpec(
  {
    val basePath = "v1/persons"
    val httpClient = IntegrationAPIHttpClient(
      HttpClient.newBuilder().build(),
      "http://localhost:4050"
    )
    val hmppsId = "2004/13116M"
    val tailPath = "risk-management-plan"
    val encodedHmppsId = URLEncoder.encode(hmppsId, StandardCharsets.UTF_8)

    it("returns a list of risk management plans using crn / hmppsId as search parameters") {

      val response = httpClient.performAuthorised("$basePath/$encodedHmppsId/$tailPath")

      response.statusCode().shouldBe(HttpStatus.OK.value())
      response.body().shouldContain("\"data\":[")
      response.body().shouldContain(
        """
          "assessmentStatus": "string",
          "assessmentType": "string",
          "initiationDate": "2024-05-04T01:04:20",
          "assessmentStatus": "string",
          "assessmentType": "string",
        """.removeWhitespaceAndNewlines(),
      )
    }
  },
)
