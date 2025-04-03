package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.assessrisksandneeds

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.FeatureNotEnabledException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.AssessRisksAndNeedsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.GeneralPredictor
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.GroupReconviction
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.RiskOfSeriousRecidivism
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.RiskPredictorScore
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.SexualPredictor
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ViolencePredictor
import java.time.LocalDateTime

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [AssessRisksAndNeedsGateway::class],
)
class GetRiskPredictorScoresForPersonTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  @MockitoBean val featureFlag: FeatureFlagConfig,
  val assessRisksAndNeedsGateway: AssessRisksAndNeedsGateway,
) : DescribeSpec(
    {
      val deliusCrn = "X777776"
      val path = "/risks/predictors/$deliusCrn"
      val assessRisksAndNeedsApiMockServer = ApiMockServer.create(UpstreamApi.ASSESS_RISKS_AND_NEEDS)

      beforeEach {
        assessRisksAndNeedsApiMockServer.start()
        Mockito.reset(hmppsAuthGateway)
        whenever(featureFlag.useArnsEndpoints).thenReturn(true)
        assessRisksAndNeedsApiMockServer.stubForGet(
          path,
          """
            [
              {
                "completedDate": "2023-09-05T10:15:41",
                "assessmentStatus": "COMPLETE",
                "groupReconvictionScore": {
                      "scoreLevel": "HIGH"
                  },
                "generalPredictorScore": {
                      "ogpRisk": "LOW"
                  },
                "violencePredictorScore": {
                      "ovpRisk": "MEDIUM"
                  },
                "riskOfSeriousRecidivismScore": {
                      "scoreLevel": "VERY_HIGH"
                  },
                "sexualPredictorScore": {
                      "ospIndecentScoreLevel": "HIGH",
                      "ospContactScoreLevel": "VERY_HIGH"
                  }
              }
            ]
          """,
        )

        Mockito.reset(hmppsAuthGateway)
        whenever(hmppsAuthGateway.getClientToken("ASSESS_RISKS_AND_NEEDS")).thenReturn(HmppsAuthMockServer.TOKEN)
      }

      afterTest {
        assessRisksAndNeedsApiMockServer.stop()
      }

      it("authenticates using HMPPS Auth with credentials") {
        assessRisksAndNeedsGateway.getRiskPredictorScoresForPerson(deliusCrn)

        verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("ASSESS_RISKS_AND_NEEDS")
      }

      it("returns risk predictor scores for the matching CRN") {
        val response = assessRisksAndNeedsGateway.getRiskPredictorScoresForPerson(deliusCrn)
        response.data.shouldBe(
          listOf(
            RiskPredictorScore(
              completedDate = LocalDateTime.parse("2023-09-05T10:15:41"),
              assessmentStatus = "COMPLETE",
              groupReconviction = GroupReconviction(scoreLevel = "HIGH"),
              generalPredictor = GeneralPredictor(scoreLevel = "LOW"),
              violencePredictor = ViolencePredictor(scoreLevel = "MEDIUM"),
              riskOfSeriousRecidivism = RiskOfSeriousRecidivism(scoreLevel = "VERY_HIGH"),
              sexualPredictor = SexualPredictor(indecentScoreLevel = "HIGH", contactScoreLevel = "VERY_HIGH"),
            ),
          ),
        )
      }

      it("returns an empty list when no risk predictor scores are found") {
        assessRisksAndNeedsApiMockServer.stubForGet(path, "[]")

        val response = assessRisksAndNeedsGateway.getRiskPredictorScoresForPerson(deliusCrn)

        response.data.shouldBe(emptyList())
      }

      it("returns a 404 NOT FOUND status code when no person is found") {
        assessRisksAndNeedsApiMockServer.stubForGet(path, "", HttpStatus.NOT_FOUND)

        val response = assessRisksAndNeedsGateway.getRiskPredictorScoresForPerson(deliusCrn)

        response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND).shouldBeTrue()
      }

      it("returns a 403 FORBIDDEN status code when forbidden") {
        assessRisksAndNeedsApiMockServer.stubForGet(path, "", HttpStatus.FORBIDDEN)

        val response = assessRisksAndNeedsGateway.getRiskPredictorScoresForPerson(deliusCrn)

        response.hasError(UpstreamApiError.Type.FORBIDDEN).shouldBeTrue()
      }

      it("returns 503 service not available when feature flag set to false") {
        whenever(featureFlag.useArnsEndpoints).thenReturn(false)
        val exception = shouldThrow<FeatureNotEnabledException> { assessRisksAndNeedsGateway.getRiskPredictorScoresForPerson(deliusCrn) }
        exception.message.shouldContain("use-arns-endpoints not enabled")
      }
    },
  )
