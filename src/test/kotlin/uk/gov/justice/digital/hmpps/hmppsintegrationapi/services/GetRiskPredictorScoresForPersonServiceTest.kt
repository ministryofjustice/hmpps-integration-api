package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.domains.risk.GetRiskPredictorScoresForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.AssessRisksAndNeedsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.GeneralPredictor
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.GroupReconviction
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.RiskOfSeriousRecidivism
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.RiskPredictorScore
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.SexualPredictor
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ViolencePredictor
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.personas.personInProbationOnlyPersona
import java.time.LocalDateTime

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetRiskPredictorScoresForPersonService::class],
)
internal class GetRiskPredictorScoresForPersonServiceTest(
    @MockitoBean val assessRisksAndNeedsGateway: AssessRisksAndNeedsGateway,
    @MockitoBean val getPersonService: GetPersonService,
    private val getRiskPredictorScoresForPersonService: GetRiskPredictorScoresForPersonService,
) : DescribeSpec(
    {
      val personFromProbationOffenderSearch =
        Person(
          firstName = personInProbationOnlyPersona.firstName,
          lastName = personInProbationOnlyPersona.lastName,
          identifiers = personInProbationOnlyPersona.identifiers,
        )

      val deliusCrn = personFromProbationOffenderSearch.identifiers.deliusCrn!!
      val hmppsId = deliusCrn

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
        verify(getPersonService, times(1)).execute(hmppsId = hmppsId)
      }

      it("gets risk predictor scores for a person from ARN API using CRN") {
        getRiskPredictorScoresForPersonService.execute(hmppsId)
        verify(assessRisksAndNeedsGateway, times(1)).getRiskPredictorScoresForPerson(deliusCrn)
      }

      it("returns risk predictor scores for a person") {
        val riskPredictors =
          listOf(
            RiskPredictorScore(
              completedDate = LocalDateTime.parse("2023-09-05T10:15:41"),
              assessmentStatus = "COMPLETE",
              generalPredictor = GeneralPredictor(scoreLevel = "LOW"),
              violencePredictor = ViolencePredictor(scoreLevel = "MEDIUM"),
              groupReconviction = GroupReconviction(scoreLevel = "HIGH"),
              riskOfSeriousRecidivism = RiskOfSeriousRecidivism(scoreLevel = "VERY_HIGH"),
              sexualPredictor = SexualPredictor(indecentScoreLevel = "HIGH", contactScoreLevel = "VERY_HIGH"),
            ),
          )
        whenever(assessRisksAndNeedsGateway.getRiskPredictorScoresForPerson(deliusCrn)).thenReturn(
          Response(data = riskPredictors),
        )

        val response = getRiskPredictorScoresForPersonService.execute(hmppsId)
        response.data.shouldBe(riskPredictors)
      }

      describe("when an upstream API returns an error") {
        describe("when a person cannot be found getPersonService") {
          val errors =
            listOf(
              UpstreamApiError(
                causedBy = UpstreamApi.PRISONER_OFFENDER_SEARCH,
                type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
              ),
            )
          beforeEach {
            whenever(getPersonService.execute(hmppsId)).thenReturn(
              Response(
                data = null,
                errors,
              ),
            )
          }

          it("records upstream 404 API error for probation offender search") {
            val response = getRiskPredictorScoresForPersonService.execute(hmppsId)
            response.errors.shouldBe(errors)
          }

          it("does not get risk predictor scores from ARN") {
            getRiskPredictorScoresForPersonService.execute(hmppsId)
            verify(assessRisksAndNeedsGateway, times(0)).getRiskPredictorScoresForPerson(id = deliusCrn)
          }
        }

        it("returns error from ARN API when person/crn cannot be found in ARN") {
          val errors =
            listOf(
              UpstreamApiError(
                causedBy = UpstreamApi.ASSESS_RISKS_AND_NEEDS,
                type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
              ),
            )
          whenever(assessRisksAndNeedsGateway.getRiskPredictorScoresForPerson(deliusCrn)).thenReturn(
            Response(
              data = emptyList(),
              errors,
            ),
          )

          val response = getRiskPredictorScoresForPersonService.execute(hmppsId)
          response.errors.shouldBe(errors)
        }
      }
    },
  )
