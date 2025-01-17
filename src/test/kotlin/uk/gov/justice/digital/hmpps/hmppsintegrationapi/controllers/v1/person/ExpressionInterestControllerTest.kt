package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.kotlin.doReturn
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
    val validHmppsId = "1234ABC"
    val invalidHmppsId = "INVALID_ID"
    val jobId = "5678"

    describe("PUT $basePath/{hmppsId}/expression-of-interest/jobs/{jobId}") {
      beforeTest {
        Mockito.reset(expressionOfInterestService, getPersonService)

        doReturn(Response(data = NomisNumber("nom1234")))
          .whenever(getPersonService).getNomisNumber(validHmppsId)
      }

      it("should return 201 Created when the expression of interest is successfully submitted") {
        val testNomis = "nom1234"
        val result = mockMvc.performAuthorised("$basePath/$validHmppsId/expression-of-interest/jobs/$jobId")

        verify(expressionOfInterestService).sendExpressionOfInterest(ExpressionInterest(jobId, testNomis))
        result.response.status.shouldBe(HttpStatus.BAD_REQUEST.value())
      }

      it("should return 400 Bad Request when the HMPPS ID is not found") {
        val result = mockMvc.performAuthorised("$basePath/$invalidHmppsId/expression-of-interest/jobs/$jobId")

        result.response.status.shouldBe(HttpStatus.BAD_REQUEST.value())
      }

      it("should return 500 Internal Server Error when an unexpected error occurs") {
        whenever(getPersonService.getNomisNumber(validHmppsId)).thenThrow(RuntimeException("Unexpected error"))
        val result = mockMvc.performAuthorised("$basePath/$validHmppsId/expression-of-interest/jobs/$jobId")

        result.response.status.shouldBe(HttpStatus.INTERNAL_SERVER_ERROR.value())
      }
    }
  })
