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
import java.time.LocalDateTime
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.GeneralPredictor as IntegrationAPIGeneralPredictor
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.GroupReconviction as IntegrationAPIGroupReconviction
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.RiskOfSeriousRecidivism as IntegrationAPIRiskOfSeriousRecidivism
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.RiskPredictorScore as IntegrationAPIRiskPredictorScore
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.SexualPredictor as IntegrationAPISexualPredictor
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
    val hmppsId = "1234/56789B"
    val nomisNumber = "Z99999ZZ"
    val deliusCrn = "X123456"

    val personFromProbationOffenderSearch =
      Person(firstName = "Phoebe", lastName = "Buffay", identifiers = Identifiers(deliusCrn = deliusCrn))

    beforeEach {
      Mockito.reset(getPersonService)
      Mockito.reset(assessRisksAndNeedsGateway)

      whenever(getPersonService.execute(hmppsId = hmppsId)).thenReturn(
        Response(
          data = personFromProbationOffenderSearch,
        ),
      )

      whenever(assessRisksAndNeedsGateway.getRiskPredictorScoresForPerson(deliusCrn)).thenReturn(Response(data = emptyList()))
    }

    it("gets a person from getPersonService") {
      getRiskPredictorScoresForPersonService.execute(hmppsId)

      verify(getPersonService, VerificationModeFactory.times(1)).execute(hmppsId = hmppsId)
    }

    it("gets risk predictor scores for a person from ARN API using CRN") {
      getRiskPredictorScoresForPersonService.execute(hmppsId)

      verify(assessRisksAndNeedsGateway, VerificationModeFactory.times(1)).getRiskPredictorScoresForPerson(deliusCrn)
    }

    it("returns risk predictor scores for a person") {
      val riskPredictors = listOf(
        IntegrationAPIRiskPredictorScore(
          completedDate = LocalDateTime.parse("2023-09-05T10:15:41"),
          assessmentStatus = "COMPLETE",
          generalPredictor = IntegrationAPIGeneralPredictor(scoreLevel = "LOW"),
          violencePredictor = IntegrationAPIViolencePredictor(scoreLevel = "MEDIUM"),
          groupReconviction = IntegrationAPIGroupReconviction(scoreLevel = "HIGH"),
          riskOfSeriousRecidivism = IntegrationAPIRiskOfSeriousRecidivism(scoreLevel = "VERY_HIGH"),
          sexualPredictor = IntegrationAPISexualPredictor(indecentScoreLevel = "HIGH", contactScoreLevel = "VERY_HIGH"),
        ),
      )
      whenever(assessRisksAndNeedsGateway.getRiskPredictorScoresForPerson(deliusCrn)).thenReturn(
        Response(data = riskPredictors),
      )

      val response = getRiskPredictorScoresForPersonService.execute(hmppsId)

      response.data.shouldBe(riskPredictors)
    }

    describe("when an upstream API returns an error") {

      xdescribe("when a person cannot be found by hmpps Id in probation offender search") {

        beforeEach {
          whenever(getPersonService.execute(hmppsId)).thenReturn(
            Response(
              data = null,
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
          val response = getRiskPredictorScoresForPersonService.execute(hmppsId)

          response.hasErrorCausedBy(UpstreamApiError.Type.ENTITY_NOT_FOUND, UpstreamApi.PROBATION_OFFENDER_SEARCH).shouldBe(true)
        }

        it("does not get risk predictor scores from ARN") {
          getRiskPredictorScoresForPersonService.execute(hmppsId)

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

        val response = getRiskPredictorScoresForPersonService.execute(hmppsId)

        response.errors.shouldHaveSize(1)
        response.errors.first().causedBy.shouldBe(UpstreamApi.ASSESS_RISKS_AND_NEEDS)
        response.errors.first().type.shouldBe(UpstreamApiError.Type.ENTITY_NOT_FOUND)
      }
    }
  },
)
