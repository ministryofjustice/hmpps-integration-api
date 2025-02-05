package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ExpressionOfInterest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.NomisNumber
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.PutExpressionInterestService

@WebMvcTest(controllers = [ExpressionInterestController::class])
@ActiveProfiles("test")
class ExpressionInterestControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @MockitoBean val getPersonService: GetPersonService,
  @MockitoBean val expressionOfInterestService: PutExpressionInterestService,
) : DescribeSpec({
    val mockMvc = IntegrationAPIMockMvc(springMockMvc)
    val basePath = "/v1/persons"
    val validHmppsId = "AABCD1ABC"
    val nomisId = "AABCD1ABC"
    val invalidHmppsId = "INVALID_ID"
    val jobId = "5678"

    describe("PUT $basePath/{hmppsId}/expression-of-interest/jobs/{jobId}") {
      it("should return 404 Not Found if ENTITY_NOT_FOUND error occurs") {
        val notFoundResponse =
          Response<NomisNumber?>(
            data = null,
            errors =
              listOf(
                UpstreamApiError(
                  type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                  description = "Entity not found",
                  causedBy = UpstreamApi.PRISONER_OFFENDER_SEARCH,
                ),
              ),
          )
        whenever(getPersonService.getNomisNumber(validHmppsId)).thenReturn(notFoundResponse)

        val result = mockMvc.performAuthorisedPut("$basePath/$validHmppsId/expression-of-interest/jobs/$jobId")
        result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
      }

      it("should throw ValidationException if an invalid hmppsId is provided") {
        val invalidResponse =
          Response<NomisNumber?>(
            data = null,
            errors =
              listOf(
                UpstreamApiError(
                  type = UpstreamApiError.Type.BAD_REQUEST,
                  description = "Invalid HmppsId",
                  causedBy = UpstreamApi.NOMIS,
                ),
              ),
          )
        whenever(getPersonService.getNomisNumber(invalidHmppsId)).thenReturn(invalidResponse)

        val result = mockMvc.performAuthorisedPut("$basePath/$invalidHmppsId/expression-of-interest/jobs/$jobId")
        result.response.status.shouldBe(HttpStatus.BAD_REQUEST.value())
      }

      it("should return 200 OK on successful expression of interest submission") {
        val validNomisResponse =
          Response<NomisNumber?>(
            data = NomisNumber(nomisId),
            errors = emptyList(),
          )
        whenever(getPersonService.getNomisNumber(validHmppsId)).thenReturn(validNomisResponse)

        val result = mockMvc.performAuthorisedPut("$basePath/$validHmppsId/expression-of-interest/jobs/$jobId")

        verify(expressionOfInterestService).sendExpressionOfInterest(ExpressionOfInterest(jobId, nomisId))
        result.response.status.shouldBe(HttpStatus.OK.value())
      }

      it("should return 500 Server Error if an exception occurs") {
        whenever(getPersonService.getNomisNumber(validHmppsId)).thenThrow(RuntimeException("Unexpected error"))

        val result = mockMvc.performAuthorisedPut("$basePath/$validHmppsId/expression-of-interest/jobs/$jobId")
        result.response.status.shouldBe(HttpStatus.INTERNAL_SERVER_ERROR.value())
      }
    }
  })
