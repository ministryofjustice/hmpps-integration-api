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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.GeneralPredictorScore
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Identifiers
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.RiskPredictor
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApiError

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetRiskPredictorsForPersonService::class],
)
internal class GetRiskPredictorsForPersonServiceTest(
  @MockBean val assessRisksAndNeedsGateway: AssessRisksAndNeedsGateway,
  @MockBean val getPersonService: GetPersonService,
  private val getRiskPredictorsForPersonService: GetRiskPredictorsForPersonService,
) : DescribeSpec(
  {
    val pncId = "1234/56789B"
    val nomisNumber = "Z99999ZZ"
    val crn = "X123456"

    val personFromPrisonOffenderSearch =
      Person(firstName = "Chandler", lastName = "Bing", identifiers = Identifiers(nomisNumber = nomisNumber))
    val personFromProbationOffenderSearch =
      Person(firstName = "Phoebe", lastName = "Buffay", identifiers = Identifiers(deliusCrn = crn))

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

      whenever(assessRisksAndNeedsGateway.getRiskPredictorsForPerson(crn)).thenReturn(Response(data = emptyList()))
    }

    it("retrieves a person from getPersonService") {
      getRiskPredictorsForPersonService.execute(pncId)

      verify(getPersonService, VerificationModeFactory.times(1)).execute(pncId = pncId)
    }

    it("retrieves risk predictors for a person from ARN API using CRN") {
      getRiskPredictorsForPersonService.execute(pncId)

      verify(assessRisksAndNeedsGateway, VerificationModeFactory.times(1)).getRiskPredictorsForPerson(crn)
    }

    it("returns risk predictors for a person") {
      val riskPredictors = listOf(
        RiskPredictor(
          generalPredictorScore = GeneralPredictorScore(80),
        ),
      )
      whenever(assessRisksAndNeedsGateway.getRiskPredictorsForPerson(crn)).thenReturn(
        Response(data = riskPredictors),
      )

      val response = getRiskPredictorsForPersonService.execute(pncId)

      response.data.shouldBe(riskPredictors)
    }

    describe("when an upstream API returns an error") {

      describe("when a person cannot be found by pnc ID in probation offender search") {

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
          val response = getRiskPredictorsForPersonService.execute(pncId)

          response.hasErrorCausedBy(UpstreamApiError.Type.ENTITY_NOT_FOUND, UpstreamApi.PROBATION_OFFENDER_SEARCH).shouldBe(true)
        }

        it("does not get risk predictors from ARN") {
          getRiskPredictorsForPersonService.execute(pncId)

          verify(assessRisksAndNeedsGateway, VerificationModeFactory.times(0)).getRiskPredictorsForPerson(id = crn)
        }
      }

      it("returns error from ARN API when person/crn cannot be found in ARN") {

        whenever(assessRisksAndNeedsGateway.getRiskPredictorsForPerson(crn)).thenReturn(
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

        val response = getRiskPredictorsForPersonService.execute(pncId)

        response.errors.shouldHaveSize(1)
        response.errors.first().causedBy.shouldBe(UpstreamApi.ARN)
        response.errors.first().type.shouldBe(UpstreamApiError.Type.ENTITY_NOT_FOUND)
      }
    }
  },
)
