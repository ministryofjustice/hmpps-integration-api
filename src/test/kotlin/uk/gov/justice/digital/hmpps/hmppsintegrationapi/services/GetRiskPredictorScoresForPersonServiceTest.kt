package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.AssessRisksAndNeedsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Identifiers
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.GeneralPredictor as IntegrationAPIGeneralPredictor
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.GroupReconviction as IntegrationAPIGroupReconviction
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.RiskPredictorScore as IntegrationAPIRiskPredictorScore
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ViolencePredictor as IntegrationAPIViolencePredictor

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetRiskPredictorScoresForPersonService::class],
)
internal class GetRiskPredictorScoresForPersonServiceTest(
  @MockBean val assessRisksAndNeedsGateway: AssessRisksAndNeedsGateway,
  @MockBean val getPersonService: GetPersonService,
  private val getRiskPredictorScoresForPersonService: GetRiskPredictorScoresForPersonService,
) : DescribeSpec(
  {
    val pncId = "1234/56789B"
    val nomisNumber = "Z99999ZZ"
    val deliusCrn = "X123456"

    val personFromPrisonOffenderSearch =
      Person(firstName = "Chandler", lastName = "Bing", identifiers = Identifiers(nomisNumber = nomisNumber))
    val personFromProbationOffenderSearch =
      Person(firstName = "Phoebe", lastName = "Buffay", identifiers = Identifiers(deliusCrn = deliusCrn))

    beforeEach {
      Mockito.reset(getPersonService)
      Mockito.reset(assessRisksAndNeedsGateway)

      whenever(getPersonService.execute(pncId = pncId)).thenReturn(
        Response(
          data = mapOf(
            "prisonerOffenderSearch" to personFromPrisonOffenderSearch,
            "probationOffenderSearch" to personFromProbationOffenderSearch,
          ),
        ),
      )

      whenever(assessRisksAndNeedsGateway.getRiskPredictorScoresForPerson(deliusCrn)).thenReturn(Response(data = emptyList()))
    }

    it("retrieves a person from getPersonService") {
      getRiskPredictorScoresForPersonService.execute(pncId)

      verify(getPersonService, VerificationModeFactory.times(1)).execute(pncId = pncId)
    }

    it("retrieves risk predictor scores for a person from ARN API using CRN") {
      getRiskPredictorScoresForPersonService.execute(pncId)

      verify(assessRisksAndNeedsGateway, VerificationModeFactory.times(1)).getRiskPredictorScoresForPerson(deliusCrn)
    }

    it("returns risk predictor scores for a person") {
      val riskPredictors = listOf(
        IntegrationAPIRiskPredictorScore(
          generalPredictor = IntegrationAPIGeneralPredictor(scoreLevel = "LOW"),
          violencePredictor = IntegrationAPIViolencePredictor(scoreLevel = "MEDIUM"),
          groupReconviction = IntegrationAPIGroupReconviction(scoreLevel = "HIGH"),
        ),
      )
      whenever(assessRisksAndNeedsGateway.getRiskPredictorScoresForPerson(deliusCrn)).thenReturn(
        Response(data = riskPredictors),
      )

      val response = getRiskPredictorScoresForPersonService.execute(pncId)

      response.data.shouldBe(riskPredictors)
    }

    describe("when an upstream API returns an error") {

      xdescribe("when a person cannot be found by pnc ID in probation offender search") {

        beforeEach {
          whenever(getPersonService.execute(pncId)).thenReturn(
            Response(
              data = mapOf(
                "prisonerOffenderSearch" to null,
                "probationOffenderSearch" to null,
              ),
              errors = listOf(
                UpstreamApiError(
                  causedBy = UpstreamApi.PRISONER_OFFENDER_SEARCH,
                  type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                ),
                UpstreamApiError(
                  causedBy = UpstreamApi.PROBATION_OFFENDER_SEARCH,
                  type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                ),
              ),
            ),
          )
        }

        it("records upstream API error for probation offender search") {
          val response = getRiskPredictorScoresForPersonService.execute(pncId)

          response.hasErrorCausedBy(UpstreamApiError.Type.ENTITY_NOT_FOUND, UpstreamApi.PROBATION_OFFENDER_SEARCH).shouldBe(true)
        }

        it("does not get risk predictor scores from ARN") {
          getRiskPredictorScoresForPersonService.execute(pncId)

          verify(assessRisksAndNeedsGateway, VerificationModeFactory.times(0)).getRiskPredictorScoresForPerson(id = deliusCrn)
        }
      }

      it("returns error from ARN API when person/crn cannot be found in ARN") {

        whenever(assessRisksAndNeedsGateway.getRiskPredictorScoresForPerson(deliusCrn)).thenReturn(
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

        val response = getRiskPredictorScoresForPersonService.execute(pncId)

        response.errors.shouldHaveSize(1)
        response.errors.first().causedBy.shouldBe(UpstreamApi.ASSESS_RISKS_AND_NEEDS)
        response.errors.first().type.shouldBe(UpstreamApiError.Type.ENTITY_NOT_FOUND)
      }
    }
  },
)
