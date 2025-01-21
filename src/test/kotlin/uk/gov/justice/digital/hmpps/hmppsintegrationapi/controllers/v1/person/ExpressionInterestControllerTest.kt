package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeOneOf
import io.kotest.matchers.shouldBe
import org.mockito.Mockito.mock
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSPrisoner
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.ExpressionInterestService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonService
import java.time.LocalDate

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
    val nomisId = "ABC789"
    val prisonerNomis = "EFG7890"
    val invalidHmppsId = "INVALID_ID"
    val jobId = "5678"
    val controller = ExpressionInterestController(mock(), mock())

    describe("PUT $basePath/{hmppsId}/expression-of-interest/jobs/{jobId}") {
      beforeTest {
        val personOnProbation =
          PersonOnProbation(
            person =
              Person(
                "Sam",
                "Smith",
                identifiers = Identifiers(nomisNumber = nomisId),
                hmppsId = validHmppsId,
                currentExclusion = true,
                exclusionMessage = "An exclusion exists",
                currentRestriction = false,
              ),
            underActiveSupervision = true,
          )

        val probationResponse = Response(data = personOnProbation, errors = emptyList())

        val prisonOffenderSearch = POSPrisoner("Kim", "Kardashian", dateOfBirth = LocalDate.of(1992, 12, 3), prisonerNumber = nomisId)
        val prisonResponse = Response(data = prisonOffenderSearch, errors = emptyList())

        val offenderMap =
          OffenderSearchResponse(
            probationOffenderSearch = probationResponse.data,
            prisonerOffenderSearch = prisonResponse.data.toPerson(),
          )

        whenever(getPersonService.getCombinedDataForPerson(validHmppsId)).thenReturn(
          Response(data = offenderMap, errors = emptyList()),
        )
      }

      it("should return 200 OK and include verifiedNomisNumberId in the Expression of Interest") {
        val result = mockMvc.performAuthorisedPut("$basePath/$validHmppsId/expression-of-interest/jobs/$jobId")

        verify(expressionOfInterestService).sendExpressionOfInterest(ExpressionInterest(jobId, nomisId))
        result.response.status.shouldBe(HttpStatus.OK.value())
      }

      it("should return 404 when an invalid hmppsId has been submitted") {
        val result = mockMvc.performAuthorisedPut("$basePath/$invalidHmppsId/expression-of-interest/jobs/$jobId")

        verify(expressionOfInterestService).sendExpressionOfInterest(ExpressionInterest(jobId, nomisId))
        result.response.status.shouldBe(HttpStatus.BAD_REQUEST.value())
      }

      it("should return 400 Bad Request when both prison and probation nomis IDs do not exist") {
        val emptyOffenderMap =
          OffenderSearchResponse(
            probationOffenderSearch = null,
            prisonerOffenderSearch = null,
          )
        whenever(getPersonService.getCombinedDataForPerson(validHmppsId)).thenReturn(
          Response(data = emptyOffenderMap, errors = emptyList()),
        )

        getPersonService.getCombinedDataForPerson(invalidHmppsId)

        val result = mockMvc.performAuthorisedPut("$basePath/$invalidHmppsId/expression-of-interest/jobs/$jobId")

        result.response.status.shouldBe(HttpStatus.BAD_REQUEST.value())
      }

      it("should return 400 Bad Request when probation nomis id does not exist and prisoner nomis id is not null") {
        val prisonOffenderSearch = POSPrisoner("Kim", "Kardashian", dateOfBirth = LocalDate.of(1992, 12, 3), prisonerNumber = prisonerNomis)
        val prisonResponse = Response(data = prisonOffenderSearch, errors = emptyList())

        val noProbationButPrisonerDataMap =
          OffenderSearchResponse(
            probationOffenderSearch = null,
            prisonerOffenderSearch = prisonResponse.data.toPerson(),
          )
        whenever(getPersonService.getCombinedDataForPerson(validHmppsId)).thenReturn(
          Response(data = noProbationButPrisonerDataMap, errors = emptyList()),
        )

        val hmppsIdCheck = getPersonService.getCombinedDataForPerson(validHmppsId)
        val verifiedNomisNumberId = controller.getNomisNumber(hmppsIdCheck)

        verifiedNomisNumberId.shouldBe(prisonerNomis)

        val result = mockMvc.performAuthorisedPut("$basePath/$invalidHmppsId/expression-of-interest/jobs/$jobId")
        result.response.status.shouldBe(HttpStatus.BAD_REQUEST.value())
      }

      it("should return 200 OK and when prisoner and probation nomis id exist to allow either of them") {
        val result = mockMvc.performAuthorisedPut("$basePath/$validHmppsId/expression-of-interest/jobs/$jobId")

        val hmppsIdCheck = getPersonService.getCombinedDataForPerson(validHmppsId)
        val verifiedNomisNumberId = controller.getNomisNumber(hmppsIdCheck)

        verifiedNomisNumberId shouldBeOneOf listOf(nomisId, prisonerNomis)
        result.response.status.shouldBe(HttpStatus.OK.value())
      }
    }
  })
