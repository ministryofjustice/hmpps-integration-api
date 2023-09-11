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
  private val getRiskPredictorsForPersonService: GetRiskPredictorsForPersonService,
) : DescribeSpec(
  {
    val crn = "X777776"

    beforeEach {
      Mockito.reset(assessRisksAndNeedsGateway)

      whenever(assessRisksAndNeedsGateway.getRiskPredictorsForPerson(crn)).thenReturn(Response(data = emptyList()))
    }

    it("retrieves risk predictors for a person from ARN API using CRN") {
      getRiskPredictorsForPersonService.execute(crn)

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

      val response = getRiskPredictorsForPersonService.execute(crn)

      response.data.shouldBe(riskPredictors)
    }

    it("returns error from ARN API when person cannot be found") {

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

      val response = getRiskPredictorsForPersonService.execute(crn)

      response.errors.shouldHaveSize(1)
      response.errors.first().causedBy.shouldBe(UpstreamApi.ARN)
      response.errors.first().type.shouldBe(UpstreamApiError.Type.ENTITY_NOT_FOUND)
    }
  },
)
