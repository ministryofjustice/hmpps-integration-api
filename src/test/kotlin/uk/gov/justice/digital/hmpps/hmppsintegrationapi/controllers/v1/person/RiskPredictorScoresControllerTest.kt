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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.GeneralPredictorScore
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.RiskPredictorScore
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetRiskPredictorScoresForPersonService
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@WebMvcTest(controllers = [RiskPredictorScoresController::class])
internal class RiskPredictorScoresControllerTest(
  @Autowired val mockMvc: MockMvc,
  @MockBean val getRiskPredictorScoresForPersonService: GetRiskPredictorScoresForPersonService,
) : DescribeSpec(
  {
    val pncId = "9999/11111A"
    val encodedPncId = URLEncoder.encode(pncId, StandardCharsets.UTF_8)
    val path = "/v1/persons/$encodedPncId/risk-predictor-scores"

    describe("GET $path") {
      beforeTest {
        Mockito.reset(getRiskPredictorScoresForPersonService)
        whenever(getRiskPredictorScoresForPersonService.execute(pncId)).thenReturn(
          Response(
            data = listOf(
              RiskPredictorScore(
                generalPredictorScore = GeneralPredictorScore(50),
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
              "generalPredictorScore": {"ogpRisk":50}
            }
          ]
        """.removeWhitespaceAndNewlines(),
        )
      }

      it("returns an empty list embedded in a JSON object when no risk predictor scores are found") {
        val pncIdForPersonWithNoRiskPredictorScores = "0000/11111A"
        val encodedPncIdForPersonWithNoRiskPredictorScores =
          URLEncoder.encode(pncIdForPersonWithNoRiskPredictorScores, StandardCharsets.UTF_8)
        val path = "/v1/persons/$encodedPncIdForPersonWithNoRiskPredictorScores/risk-predictor-scores"

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
              RiskPredictorScore(
                generalPredictorScore = GeneralPredictorScore(60),
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