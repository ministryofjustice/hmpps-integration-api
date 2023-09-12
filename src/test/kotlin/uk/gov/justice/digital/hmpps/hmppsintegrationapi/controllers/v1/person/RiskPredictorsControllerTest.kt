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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.RiskPredictor
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetRiskPredictorsForPersonService
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@WebMvcTest(controllers = [RiskPredictorsController::class])
internal class RiskPredictorsControllerTest(
  @Autowired val mockMvc: MockMvc,
  @MockBean val getRiskPredictorsForPersonService: GetRiskPredictorsForPersonService,
) : DescribeSpec(
  {
    val pncId = "9999/11111A"
    val encodedPncId = URLEncoder.encode(pncId, StandardCharsets.UTF_8)
    val path = "/v1/persons/$encodedPncId/risk-predictors"

    describe("GET $path") {
      beforeTest {
        Mockito.reset(getRiskPredictorsForPersonService)
        whenever(getRiskPredictorsForPersonService.execute(pncId)).thenReturn(
          Response(
            data = listOf(
              RiskPredictor(
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

      it("retrieves the risk predictors for a person with the matching ID") {
        mockMvc.perform(MockMvcRequestBuilders.get("$path")).andReturn()

        verify(getRiskPredictorsForPersonService, VerificationModeFactory.times(1)).execute(pncId)
      }

      it("returns the risk predictors for a person with the matching ID") {
        val result = mockMvc.perform(MockMvcRequestBuilders.get("$path")).andReturn()

        result.response.contentAsString.shouldContain(
          """
          "data": [
            {
              "generalPredictorScore": {"totalWeightedScore":50}
            }
          ]
        """.removeWhitespaceAndNewlines(),
        )
      }

      it("returns an empty list embedded in a JSON object when no risk predictors are found") {
        val pncIdForPersonWithNoRiskPredictors = "0000/11111A"
        val encodedPncIdForPersonWithNoRiskPredictors =
          URLEncoder.encode(pncIdForPersonWithNoRiskPredictors, StandardCharsets.UTF_8)
        val path = "/v1/persons/$encodedPncIdForPersonWithNoRiskPredictors/risk-predictors"

        whenever(getRiskPredictorsForPersonService.execute(pncIdForPersonWithNoRiskPredictors)).thenReturn(
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
        whenever(getRiskPredictorsForPersonService.execute(pncId)).thenReturn(
          Response(
            data = emptyList(),
            errors = listOf(
              UpstreamApiError(
                causedBy = UpstreamApi.ARN,
                type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
              ),
            ),
          ),
        )

        val result = mockMvc.perform(MockMvcRequestBuilders.get("$path")).andReturn()

        result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
      }

      it("returns paginated results") {
        whenever(getRiskPredictorsForPersonService.execute(pncId)).thenReturn(
          Response(
            data =
            List(30) {
              RiskPredictor(
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
