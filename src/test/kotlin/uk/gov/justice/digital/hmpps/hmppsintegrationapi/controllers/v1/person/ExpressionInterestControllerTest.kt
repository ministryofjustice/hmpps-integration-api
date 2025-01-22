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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.expressionOfInterest.ExpressionInterest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.NomisNumber
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.ExpressionInterestService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonService

@WebMvcTest(controllers = [ExpressionInterestController::class])
@ActiveProfiles("test")
class ExpressionInterestControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @MockitoBean val getPersonService: GetPersonService,
  @MockitoBean val expressionOfInterestService: ExpressionInterestService,
) : DescribeSpec({
    val mockMvc = IntegrationAPIMockMvc(springMockMvc)
    val basePath = "/v1/persons"
    val validHmppsId = "AABCD1ABC"
    val nomisId = "AABCD1ABC"
    val invalidHmppsId = "INVALID_ID"
    val jobId = "5678"

    describe("PUT $basePath/{hmppsId}/expression-of-interest/jobs/{jobId}") {
      it("should return 400 Bad Request if ENTITY_NOT_FOUND error occurs") {
        val notFoundResponse =
          Response<NomisNumber?>(
            data = null,
            errors = emptyList(),
          )
        whenever(getPersonService.getNomisNumber(validHmppsId)).thenReturn(notFoundResponse)

        val result = mockMvc.performAuthorisedPut("$basePath/$validHmppsId/expression-of-interest/jobs/$jobId")

        result.response.status.shouldBe(HttpStatus.BAD_REQUEST.value())
      }

      it("should return 400 if an invalid hmppsId is provided") {
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

        verify(expressionOfInterestService).sendExpressionOfInterest(ExpressionInterest(jobId, nomisId))
        result.response.status.shouldBe(HttpStatus.OK.value())
      }

      it("should return 400 Bad Request if an exception occurs") {
        whenever(getPersonService.getNomisNumber(validHmppsId)).thenThrow(RuntimeException("Unexpected error"))

        val result = mockMvc.performAuthorisedPut("$basePath/$validHmppsId/expression-of-interest/jobs/$jobId")

        result.response.status.shouldBe(HttpStatus.BAD_REQUEST.value())
      }
    }
  })
