package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Identifiers
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.OffenderSearchResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonOnProbation
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSPrisoner
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.ExpressionInterestService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonService

@WebMvcTest(controllers = [ExpressionInterestController::class])
@ActiveProfiles("test")
class ExpressionInterestControllerTest(
  @Autowired private val springMockMvc: MockMvc,
  @MockitoBean private val getPersonService: GetPersonService,
  @MockitoBean private val expressionOfInterestService: ExpressionInterestService,
) : DescribeSpec({

    val mockMvc = IntegrationAPIMockMvc(springMockMvc)
    val basePath = "/v1/persons"
    val validHmppsId = "1234ABC"
    val invalidHmppsId = "INVALID_ID"
    val jobId = "5678"

    describe("PUT $basePath/{hmppsId}/expression-of-interest/jobs/{jobId}") {

      beforeTest {
        Mockito.reset(getPersonService, expressionOfInterestService)
      }

      it("should return 201 Created when the expression of interest is successfully submitted") {
        val personOnProbation =
          PersonOnProbation(
            person =
              Person(
                "Sam",
                "Smith",
                identifiers = Identifiers(nomisNumber = validHmppsId),
                hmppsId = validHmppsId,
                currentExclusion = true,
                exclusionMessage = "An exclusion exists",
                currentRestriction = false,
              ),
            underActiveSupervision = true,
          )

        val probationResponse = Response(data = personOnProbation, errors = emptyList())

        val prisonOffenderSearch = POSPrisoner("Kim", "Kardashian")
        val prisonResponse = Response(data = prisonOffenderSearch, errors = emptyList())

        val offenderMap =
          OffenderSearchResponse(
            probationOffenderSearch = probationResponse.data,
            prisonerOffenderSearch = prisonResponse.data.toPerson(),
          )

        whenever(getPersonService.getCombinedDataForPerson(validHmppsId)).thenReturn(
          Response(data = offenderMap, errors = emptyList()),
        )

        val result = mockMvc.performAuthorised("$basePath/$validHmppsId/expression-of-interest/jobs/$jobId")

        result.response.status.shouldBe(HttpStatus.CREATED.value())
        verify(expressionOfInterestService).sendExpressionOfInterest(ExpressionInterest(jobId, validHmppsId))
      }

      it("should return 400 Bad Request when the HMPPS ID is not found") {
        whenever(getPersonService.getCombinedDataForPerson(invalidHmppsId)).thenReturn(
          Response(
            data =
              OffenderSearchResponse(
                probationOffenderSearch = null,
                prisonerOffenderSearch = null,
              ),
            errors =
              listOf(
                UpstreamApiError(
                  type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                  causedBy = UpstreamApi.PROBATION_OFFENDER_SEARCH,
                ),
              ),
          ),
        )

        val result = mockMvc.performAuthorised("$basePath/$invalidHmppsId/expression-of-interest/jobs/$jobId")

        result.response.status.shouldBe(HttpStatus.BAD_REQUEST.value())
      }

      it("should return 500 Internal Server Error when an unexpected error occurs") {
        whenever(getPersonService.getCombinedDataForPerson(validHmppsId)).thenThrow(RuntimeException("Unexpected error"))

        val result = mockMvc.performAuthorised("$basePath/$validHmppsId/expression-of-interest/jobs/$jobId")

        result.response.status.shouldBe(HttpStatus.INTERNAL_SERVER_ERROR.value())
      }
    }
  })
