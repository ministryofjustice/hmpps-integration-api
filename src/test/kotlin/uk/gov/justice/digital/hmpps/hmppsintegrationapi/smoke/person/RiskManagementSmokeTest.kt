package uk.gov.justice.digital.hmpps.hmppsintegrationapi.smoke.person

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIHttpClient
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class RiskManagementSmokeTest : DescribeSpec(
  {
    val basePath = "v1/persons"
    val httpClient = IntegrationAPIHttpClient()
    val hmppsId = "D1974X"
    val tailPath = "risk-management-plan"
    val encodedHmppsId = URLEncoder.encode(hmppsId, StandardCharsets.UTF_8)

    it("returns a list of risk management plans using crn / hmppsId as search parameters") {

      val response = httpClient.performAuthorised("$basePath/$encodedHmppsId/$tailPath")

      response.statusCode().shouldBe(HttpStatus.OK.value())
      response.body().shouldContain(
        """
          {"data":[{"assessmentId":"-9007199254740991",
          "dateCompleted":"2024-05-08T23:11:23",
          "initiationDate":"2024-05-08T23:11:23",
          "assessmentStatus":"string",
          "assessmentType":"string",
          "keyInformationCurrentSituation":"string",
          "furtherConsiderationsCurrentSituation":"string",
          "supervision":"string",
          "monitoringAndControl":"string",
          "interventionsAndTreatment":"string",
          "victimSafetyPlanning":"string",
          "contingencyPlans":"string",
          "latestSignLockDate":"2024-05-08T23:11:23",
          "latestCompleteDate":"2024-05-08T23:11:23"}],
          "pagination":{"isLastPage":true,
          "count":1,
          "page":1,
          "perPage":10,
          "totalCount":1,
          "totalPages":1}}
        """.removeWhitespaceAndNewlines(),
      )
    }
  },
)
