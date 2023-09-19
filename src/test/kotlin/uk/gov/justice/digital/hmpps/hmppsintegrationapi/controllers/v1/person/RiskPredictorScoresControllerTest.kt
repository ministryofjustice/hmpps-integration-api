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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ViolencePredictor as IntegrationAPIViolencePredictor

@WebMvcTest(controllers = [RiskPredictorScoresController::class])
internal class RiskPredictorScoresControllerTest(
  @Autowired val mockMvc: MockMvc,
  @MockBean val getRiskPredictorScoresForPersonService: GetRiskPredictorScoresForPersonService,
) : DescribeSpec(
  {
    val pncId = "9999/11111A"
    val encodedPncId = URLEncoder.encode(pncId, StandardCharsets.UTF_8)
    val path = "/v1/persons/$encodedPncId/risks/scores"

    describe("GET $path") {
      beforeTest {
        Mockito.reset(getRiskPredictorScoresForPersonService)
        whenever(getRiskPredictorScoresForPersonService.execute(pncId)).thenReturn(
          Response(
            data = listOf(
              IntegrationAPIRiskPredictorScore(
                completedDate = LocalDateTime.parse("2023-09-05T10:15:41"),
                assessmentStatus = "COMPLETE",
                generalPredictor = IntegrationAPIGeneralPredictor("HIGH"),
                violencePredictor = IntegrationAPIViolencePredictor("MEDIUM"),
                groupReconviction = IntegrationAPIGroupReconviction("LOW"),
                riskOfSeriousRecidivism = IntegrationAPIRiskOfSeriousRecidivism(scoreLevel = "VERY_HIGH"),
              ),
            ),
          ),
        )
      }

      it("responds with a 200 OK status") {
        val result = mockMvc.perform(MockMvcRequestBuilders.get("$path")).andReturn()

        result.response.status.shouldBe(HttpStatus.OK.value())
      }

      it("retrieves the risk predictor scores for a person with the matching ID") {
        mockMvc.perform(MockMvcRequestBuilders.get("$path")).andReturn()

        verify(getRiskPredictorScoresForPersonService, VerificationModeFactory.times(1)).execute(pncId)
      }

      it("returns the risk predictor scores for a person with the matching ID") {
        val result = mockMvc.perform(MockMvcRequestBuilders.get("$path")).andReturn()

        result.response.contentAsString.shouldContain(
          """
          "data": [
            {
              "completedDate": "2023-09-05T10:15:41",
              "assessmentStatus": "COMPLETE",
              "generalPredictor": {"scoreLevel":"HIGH"},
              "violencePredictor": {"scoreLevel":"MEDIUM"},
              "groupReconviction": {"scoreLevel":"LOW"},
              "riskOfSeriousRecidivism": {"scoreLevel":"VERY_HIGH"}
            }
          ]
        """.removeWhitespaceAndNewlines(),
        )
      }

      it("returns an empty list embedded in a JSON object when no risk predictor scores are found") {
        val pncIdForPersonWithNoRiskPredictorScores = "0000/11111A"
        val encodedPncIdForPersonWithNoRiskPredictorScores =
          URLEncoder.encode(pncIdForPersonWithNoRiskPredictorScores, StandardCharsets.UTF_8)
        val path = "/v1/persons/$encodedPncIdForPersonWithNoRiskPredictorScores/risks/scores"

        whenever(getRiskPredictorScoresForPersonService.execute(pncIdForPersonWithNoRiskPredictorScores)).thenReturn(
          Response(
            data = emptyList(),
          ),
        )

        val result =
          mockMvc.perform(MockMvcRequestBuilders.get("$path"))
            .andReturn()

        result.response.contentAsString.shouldContain("\"data\":[]")
      }

      it("responds with a 404 NOT FOUND status when person isn't found in the upstream API") {
        whenever(getRiskPredictorScoresForPersonService.execute(pncId)).thenReturn(
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

        val result = mockMvc.perform(MockMvcRequestBuilders.get("$path")).andReturn()

        result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
      }

      it("returns paginated results") {
        whenever(getRiskPredictorScoresForPersonService.execute(pncId)).thenReturn(
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
              )
            },
          ),
        )

        val result =
          mockMvc.perform(MockMvcRequestBuilders.get("$path?page=1&perPage=10"))
            .andReturn()

        result.response.contentAsString.shouldContainJsonKeyValue("$.pagination.page", 1)
        result.response.contentAsString.shouldContainJsonKeyValue("$.pagination.totalPages", 3)
      }
    }
  },
)
