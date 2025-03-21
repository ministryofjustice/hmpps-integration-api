package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import jakarta.validation.ValidationException
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.GetCaseAccess
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.HmppsIdConverter
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.PutExpressionInterestService

@WebMvcTest(controllers = [ExpressionInterestController::class])
@ActiveProfiles("test")
class ExpressionInterestControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @MockitoBean val expressionOfInterestService: PutExpressionInterestService,
  @MockitoBean val hmppsIdConverter: HmppsIdConverter,
  @MockitoBean val getCaseAccess: GetCaseAccess,
) : DescribeSpec({
    val mockMvc = IntegrationAPIMockMvc(springMockMvc)
    val basePath = "/v1/persons"
    val validHmppsId = "AABCD1ABC"
    val invalidHmppsId = "INVALID_ID"
    val jobId = "5678"

    describe("PUT $basePath/{hmppsId}/expression-of-interest/jobs/{jobId}") {
      it("should return 404 Not Found if ENTITY_NOT_FOUND error occurs") {
        validHmppsId.let { id ->
          whenever(expressionOfInterestService.sendExpressionOfInterest(id, jobId)).thenThrow(EntityNotFoundException("Could not find person with id: $id"))
        }

        val result = mockMvc.performAuthorisedPut("$basePath/$validHmppsId/expression-of-interest/jobs/$jobId")
        result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
      }

      it("should throw ValidationException if an invalid hmppsId is provided") {
        invalidHmppsId.let { id ->
          whenever(expressionOfInterestService.sendExpressionOfInterest(id, jobId)).thenThrow(ValidationException("Invalid HMPPS ID: $id"))
        }

        val result = mockMvc.performAuthorisedPut("$basePath/$invalidHmppsId/expression-of-interest/jobs/$jobId")
        result.response.status.shouldBe(HttpStatus.BAD_REQUEST.value())
      }

      it("should return 200 OK on successful expression of interest submission") {
        validHmppsId.let { id ->
          doNothing().whenever(expressionOfInterestService).sendExpressionOfInterest(id, jobId)
        }

        val result = mockMvc.performAuthorisedPut("$basePath/$validHmppsId/expression-of-interest/jobs/$jobId")
        result.response.status.shouldBe(HttpStatus.OK.value())
      }

      it("should return 500 Server Error if an exception occurs") {
        whenever(expressionOfInterestService.sendExpressionOfInterest(any(), any())).thenThrow(RuntimeException("Unexpected error"))

        val result = mockMvc.performAuthorisedPut("$basePath/$validHmppsId/expression-of-interest/jobs/$jobId")
        result.response.status.shouldBe(HttpStatus.INTERNAL_SERVER_ERROR.value())
      }
    }
  })
