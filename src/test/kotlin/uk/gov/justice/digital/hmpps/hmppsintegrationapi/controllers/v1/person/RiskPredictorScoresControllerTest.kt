package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import io.kotest.assertions.json.shouldContainJsonKeyValue
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.MockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetRiskPredictorScoresForPersonService
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.GeneralPredictor as IntegrationAPIGeneralPredictor
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.GroupReconviction as IntegrationAPIGroupReconviction
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.RiskOfSeriousRecidivism as IntegrationAPIRiskOfSeriousRecidivism
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.RiskPredictorScore as IntegrationAPIRiskPredictorScore
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.SexualPredictor as IntegrationAPISexualPredictor
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ViolencePredictor as IntegrationAPIViolencePredictor

@WebMvcTest(controllers = [RiskPredictorScoresController::class])
internal class RiskPredictorScoresControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @MockBean val getRiskPredictorScoresForPersonService: GetRiskPredictorScoresForPersonService,
) : DescribeSpec(
  {
    val hmppsId = "9999/11111A"
    val encodedHmppsId = URLEncoder.encode(hmppsId, StandardCharsets.UTF_8)
    val path = "/v1/persons/$encodedHmppsId/risks/scores"
    val mockMvc = IntegrationAPIMockMvc(springMockMvc)

    describe("GET $path") {
      beforeTest {
        Mockito.reset(getRiskPredictorScoresForPersonService)
        whenever(getRiskPredictorScoresForPersonService.execute(hmppsId)).thenReturn(
          Response(
            data = listOf(
              IntegrationAPIRiskPredictorScore(
                completedDate = LocalDateTime.parse("2023-09-05T10:15:41"),
                assessmentStatus = "COMPLETE",
                generalPredictor = IntegrationAPIGeneralPredictor("HIGH"),
                violencePredictor = IntegrationAPIViolencePredictor("MEDIUM"),
                groupReconviction = IntegrationAPIGroupReconviction("LOW"),
                riskOfSeriousRecidivism = IntegrationAPIRiskOfSeriousRecidivism(scoreLevel = "VERY_HIGH"),
                sexualPredictor = IntegrationAPISexualPredictor(indecentScoreLevel = "HIGH", contactScoreLevel = "VERY_HIGH"),
              ),
            ),
          ),
        )
      }

      it("responds with a 200 OK status") {
        val result = mockMvc.performAuthorised(path)

        result.response.status.shouldBe(HttpStatus.OK.value())
      }

      it("retrieves the risk predictor scores for a person with the matching ID") {
        mockMvc.performAuthorised(path)

        verify(getRiskPredictorScoresForPersonService, VerificationModeFactory.times(1)).execute(hmppsId)
      }

      it("returns the risk predictor scores for a person with the matching ID") {
        val result = mockMvc.performAuthorised(path)

        result.response.contentAsString.shouldContain(
          """
          "data": [
            {
              "completedDate": "2023-09-05T10:15:41",
              "assessmentStatus": "COMPLETE",
              "generalPredictor": {"scoreLevel":"HIGH"},
              "violencePredictor": {"scoreLevel":"MEDIUM"},
              "groupReconviction": {"scoreLevel":"LOW"},
              "riskOfSeriousRecidivism": {"scoreLevel":"VERY_HIGH"},
              "sexualPredictor": {
                "indecentScoreLevel":"HIGH",
                "contactScoreLevel":"VERY_HIGH"
              }
            }
          ]
        """.removeWhitespaceAndNewlines(),
        )
      }

      it("returns an empty list embedded in a JSON object when no risk predictor scores are found") {
        val hmppsIdForPersonWithNoRiskPredictorScores = "0000/11111A"
        val encodedHmppsIdForPersonWithNoRiskPredictorScores =
          URLEncoder.encode(hmppsIdForPersonWithNoRiskPredictorScores, StandardCharsets.UTF_8)
        val path = "/v1/persons/$encodedHmppsIdForPersonWithNoRiskPredictorScores/risks/scores"

        whenever(getRiskPredictorScoresForPersonService.execute(hmppsIdForPersonWithNoRiskPredictorScores)).thenReturn(
          Response(
            data = emptyList(),
          ),
        )

        val result = mockMvc.performAuthorised(path)

        result.response.contentAsString.shouldContain("\"data\":[]")
      }

      it("responds with a 404 NOT FOUND status when person isn't found in the upstream API") {
        whenever(getRiskPredictorScoresForPersonService.execute(hmppsId)).thenReturn(
          Response(
            data = emptyList(),
            errors = listOf(
              UpstreamApiError(
                causedBy = UpstreamApi.ASSESS_RISKS_AND_NEEDS,
                type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
              ),
            ),
          ),
        )

        val result = mockMvc.performAuthorised(path)

        result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
      }

      it("returns paginated results") {
        whenever(getRiskPredictorScoresForPersonService.execute(hmppsId)).thenReturn(
          Response(
            data =
            List(30) {
              IntegrationAPIRiskPredictorScore(
                completedDate = LocalDateTime.parse("2023-09-05T10:15:41"),
                assessmentStatus = "COMPLETE",
                generalPredictor = IntegrationAPIGeneralPredictor("HIGH"),
                violencePredictor = IntegrationAPIViolencePredictor("MEDIUM"),
                groupReconviction = IntegrationAPIGroupReconviction("LOW"),
                riskOfSeriousRecidivism = IntegrationAPIRiskOfSeriousRecidivism(scoreLevel = "VERY_HIGH"),
                sexualPredictor = IntegrationAPISexualPredictor(indecentScoreLevel = "HIGH", contactScoreLevel = "VERY_HIGH"),
              )
            },
          ),
        )

        val result = mockMvc.performAuthorised("$path?page=1&perPage=10")

        result.response.contentAsString.shouldContainJsonKeyValue("$.pagination.page", 1)
        result.response.contentAsString.shouldContainJsonKeyValue("$.pagination.totalPages", 3)
      }
    }
  },
)
