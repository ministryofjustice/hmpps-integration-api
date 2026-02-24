package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.assessrisksandneeds

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.assertThrows
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.AssessRisksAndNeedsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.GeneralPredictor
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.GroupReconviction
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.RiskOfSeriousRecidivism
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.RiskPredictorScore
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.RiskScoreV2
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.SexualPredictor
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ViolencePredictor
import java.io.File
import java.math.BigDecimal
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
      val deliusCrnNewV1 = "X777777"
      val deliusCrnNewV2 = "X777778"
      val deliusCrnNewV1AndV2 = "X777779"
      val pathNewV1 = "/risks/predictors/unsafe/all/CRN/$deliusCrnNewV1"
      val pathNewV2 = "/risks/predictors/unsafe/all/CRN/$deliusCrnNewV2"
      var pathNewV1AndV2 = "/risks/predictors/unsafe/all/CRN/$deliusCrnNewV1AndV2"
      val assessRisksAndNeedsApiMockServer = ApiMockServer.create(UpstreamApi.ASSESS_RISKS_AND_NEEDS)

      beforeEach {
        assessRisksAndNeedsApiMockServer.start()
        Mockito.reset(hmppsAuthGateway)

        assessRisksAndNeedsApiMockServer.stubForGet(
          pathNewV1,
          File("src/test/resources/expected-responses/arns-risk-predictor-scores-new-v1-only.json").readText(),
        )

        assessRisksAndNeedsApiMockServer.stubForGet(
          pathNewV2,
          File("src/test/resources/expected-responses/arns-risk-predictor-scores-new-v2-only.json").readText(),
        )

        assessRisksAndNeedsApiMockServer.stubForGet(
          pathNewV1AndV2,
          File("src/test/resources/expected-responses/arns-risk-predictor-scores-new-v1-and-v2.json").readText(),
        )

        Mockito.reset(hmppsAuthGateway)
        whenever(hmppsAuthGateway.getClientToken("ASSESS_RISKS_AND_NEEDS")).thenReturn(HmppsAuthMockServer.TOKEN)
      }

      afterTest {
        assessRisksAndNeedsApiMockServer.stop()
      }

      it("authenticates using HMPPS Auth with credentials") {
        assessRisksAndNeedsGateway.getRiskPredictorScoresForPerson(deliusCrnNewV1)

        verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("ASSESS_RISKS_AND_NEEDS")
      }

      it("returns risk predictor scores for the matching CRN with version 1") {
        val response = assessRisksAndNeedsGateway.getRiskPredictorScoresForPerson(deliusCrnNewV1)

        response.data.shouldBe(
          listOf(
            RiskPredictorScore(
              completedDate = LocalDateTime.parse("2026-01-16T16:22:54"),
              assessmentStatus = "COMPLETE",
              groupReconviction = GroupReconviction(scoreLevel = "HIGH", score = BigDecimal(1)),
              generalPredictor = GeneralPredictor(scoreLevel = "LOW", score = BigDecimal(11)),
              violencePredictor = ViolencePredictor(scoreLevel = "MEDIUM", score = BigDecimal(6)),
              riskOfSeriousRecidivism = RiskOfSeriousRecidivism(scoreLevel = "LOW", score = BigDecimal(12)),
              sexualPredictor = SexualPredictor(indecentScoreLevel = "LOW", contactScoreLevel = "MEDIUM", contactScore = BigDecimal(16), indecentScore = BigDecimal(15)),
              assessmentVersion = 1,
              allReoffendingPredictor = null,
              violentReoffendingPredictor = null,
              seriousViolentReoffendingPredictor = null,
              directContactSexualReoffendingPredictor = null,
              indirectImageContactSexualReoffendingPredictor = null,
              combinedSeriousReoffendingPredictor = null,
            ),
          ),
        )
      }

      it("returns risk predictor scores for the matching CRN with version 1 when send decimals is enabled") {

        assessRisksAndNeedsApiMockServer.stubForGet(
          pathNewV1,
          File("src/test/resources/expected-responses/arns-risk-predictor-scores-new-v1-only-decimals.json").readText(),
        )
        whenever(featureFlag.isEnabled(FeatureFlagConfig.ENABLE_SEND_DECIMAL_RISK_SCORES)).thenReturn(true)
        val response = assessRisksAndNeedsGateway.getRiskPredictorScoresForPerson(deliusCrnNewV1)

        response.data.shouldBe(
          listOf(
            RiskPredictorScore(
              completedDate = LocalDateTime.parse("2026-01-16T16:22:54"),
              assessmentStatus = "COMPLETE",
              groupReconviction = GroupReconviction(scoreLevel = "HIGH", score = BigDecimal(1)),
              generalPredictor = GeneralPredictor(scoreLevel = "LOW", score = BigDecimal("11.12")),
              violencePredictor = ViolencePredictor(scoreLevel = "MEDIUM", score = BigDecimal("6.1")),
              riskOfSeriousRecidivism = RiskOfSeriousRecidivism(scoreLevel = "LOW", score = BigDecimal("12.17")),
              sexualPredictor = SexualPredictor(indecentScoreLevel = "LOW", contactScoreLevel = "MEDIUM", contactScore = BigDecimal("16.2"), indecentScore = BigDecimal("15.2")),
              assessmentVersion = 1,
              allReoffendingPredictor = null,
              violentReoffendingPredictor = null,
              seriousViolentReoffendingPredictor = null,
              directContactSexualReoffendingPredictor = null,
              indirectImageContactSexualReoffendingPredictor = null,
              combinedSeriousReoffendingPredictor = null,
            ),
          ),
        )
      }

      it("returns risk predictor scores for the matching CRN with version 2") {
        val response = assessRisksAndNeedsGateway.getRiskPredictorScoresForPerson(deliusCrnNewV2)

        response.data.shouldBe(
          listOf(
            RiskPredictorScore(
              completedDate = LocalDateTime.parse("2026-01-16T16:22:54"),
              assessmentStatus = "COMPLETE",
              assessmentVersion = 2,
              allReoffendingPredictor = RiskScoreV2(band = "LOW", score = BigDecimal(1)),
              violentReoffendingPredictor = RiskScoreV2(band = "MEDIUM", score = BigDecimal(30)),
              seriousViolentReoffendingPredictor = RiskScoreV2(band = "HIGH", score = BigDecimal(99)),
              directContactSexualReoffendingPredictor = RiskScoreV2(band = "LOW", score = BigDecimal(10)),
              indirectImageContactSexualReoffendingPredictor = RiskScoreV2(band = "LOW", score = BigDecimal(10)),
              combinedSeriousReoffendingPredictor = RiskScoreV2(band = "LOW", score = BigDecimal(0)),
              groupReconviction = GroupReconviction(),
              generalPredictor = GeneralPredictor(),
              violencePredictor = ViolencePredictor(),
              riskOfSeriousRecidivism = RiskOfSeriousRecidivism(),
              sexualPredictor = SexualPredictor(),
            ),
          ),
        )
      }

      it("returns risk predictor scores for the matching CRN with version 2 when send decimals is enabled") {
        assessRisksAndNeedsApiMockServer.stubForGet(
          pathNewV2,
          File("src/test/resources/expected-responses/arns-risk-predictor-scores-new-v2-only-decimals.json").readText(),
        )
        whenever(featureFlag.isEnabled(FeatureFlagConfig.ENABLE_SEND_DECIMAL_RISK_SCORES)).thenReturn(true)
        val response = assessRisksAndNeedsGateway.getRiskPredictorScoresForPerson(deliusCrnNewV2)

        response.data.shouldBe(
          listOf(
            RiskPredictorScore(
              completedDate = LocalDateTime.parse("2026-01-16T16:22:54"),
              assessmentStatus = "COMPLETE",
              assessmentVersion = 2,
              allReoffendingPredictor = RiskScoreV2(band = "LOW", score = BigDecimal(1)),
              violentReoffendingPredictor = RiskScoreV2(band = "MEDIUM", score = BigDecimal("30.2")),
              seriousViolentReoffendingPredictor = RiskScoreV2(band = "HIGH", score = BigDecimal("99.9")),
              directContactSexualReoffendingPredictor = RiskScoreV2(band = "LOW", score = BigDecimal("10.1")),
              indirectImageContactSexualReoffendingPredictor = RiskScoreV2(band = "LOW", score = BigDecimal("10.3")),
              combinedSeriousReoffendingPredictor = RiskScoreV2(band = "LOW", score = BigDecimal("0.3")),
              groupReconviction = GroupReconviction(),
              generalPredictor = GeneralPredictor(),
              violencePredictor = ViolencePredictor(),
              riskOfSeriousRecidivism = RiskOfSeriousRecidivism(),
              sexualPredictor = SexualPredictor(),
            ),
          ),
        )
      }

      it("returns risk predictor scores for the matching CRN with version 1 and version 2") {
        val response = assessRisksAndNeedsGateway.getRiskPredictorScoresForPerson(deliusCrnNewV1AndV2)

        response.data.shouldBe(
          listOf(
            RiskPredictorScore(
              completedDate = LocalDateTime.parse("2026-01-16T16:22:54"),
              assessmentStatus = "COMPLETE",
              assessmentVersion = 2,
              allReoffendingPredictor = RiskScoreV2(band = "LOW", score = BigDecimal(1)),
              violentReoffendingPredictor = RiskScoreV2(band = "MEDIUM", score = BigDecimal(30)),
              seriousViolentReoffendingPredictor = RiskScoreV2(band = "HIGH", score = BigDecimal(99)),
              directContactSexualReoffendingPredictor = RiskScoreV2(band = "LOW", score = BigDecimal(10)),
              indirectImageContactSexualReoffendingPredictor = RiskScoreV2(band = "LOW", score = BigDecimal(10)),
              combinedSeriousReoffendingPredictor = RiskScoreV2(band = "LOW", score = BigDecimal(0)),
              groupReconviction = GroupReconviction(),
              generalPredictor = GeneralPredictor(),
              violencePredictor = ViolencePredictor(),
              riskOfSeriousRecidivism = RiskOfSeriousRecidivism(),
              sexualPredictor = SexualPredictor(),
            ),
            RiskPredictorScore(
              completedDate = LocalDateTime.parse("2026-01-16T16:22:54"),
              assessmentStatus = "COMPLETE",
              groupReconviction = GroupReconviction(scoreLevel = "HIGH", score = BigDecimal(1)),
              generalPredictor = GeneralPredictor(scoreLevel = "LOW", score = BigDecimal(11)),
              violencePredictor = ViolencePredictor(scoreLevel = "MEDIUM", score = BigDecimal(6)),
              riskOfSeriousRecidivism = RiskOfSeriousRecidivism(scoreLevel = "LOW", score = BigDecimal(12)),
              sexualPredictor = SexualPredictor(indecentScoreLevel = "LOW", contactScoreLevel = "MEDIUM", contactScore = BigDecimal(16), indecentScore = BigDecimal(15)),
              assessmentVersion = 1,
              allReoffendingPredictor = null,
              violentReoffendingPredictor = null,
              seriousViolentReoffendingPredictor = null,
              directContactSexualReoffendingPredictor = null,
              indirectImageContactSexualReoffendingPredictor = null,
              combinedSeriousReoffendingPredictor = null,
            ),
          ),
        )
      }

      it("returns risk predictor scores for the matching CRN with version 1 and version 2 with decimals enabled") {
        assessRisksAndNeedsApiMockServer.stubForGet(
          pathNewV1AndV2,
          File("src/test/resources/expected-responses/arns-risk-predictor-scores-new-v1-and-v2-decimals.json").readText(),
        )
        whenever(featureFlag.isEnabled(FeatureFlagConfig.ENABLE_SEND_DECIMAL_RISK_SCORES)).thenReturn(true)
        val response = assessRisksAndNeedsGateway.getRiskPredictorScoresForPerson(deliusCrnNewV1AndV2)

        response.data.shouldBe(
          listOf(
            RiskPredictorScore(
              completedDate = LocalDateTime.parse("2026-01-16T16:22:54"),
              assessmentStatus = "COMPLETE",
              assessmentVersion = 2,
              allReoffendingPredictor = RiskScoreV2(band = "LOW", score = BigDecimal(1)),
              violentReoffendingPredictor = RiskScoreV2(band = "MEDIUM", score = BigDecimal("30.2")),
              seriousViolentReoffendingPredictor = RiskScoreV2(band = "HIGH", score = BigDecimal("99.9")),
              directContactSexualReoffendingPredictor = RiskScoreV2(band = "LOW", score = BigDecimal("10.1")),
              indirectImageContactSexualReoffendingPredictor = RiskScoreV2(band = "LOW", score = BigDecimal("10.3")),
              combinedSeriousReoffendingPredictor = RiskScoreV2(band = "LOW", score = BigDecimal("0.3")),
              groupReconviction = GroupReconviction(),
              generalPredictor = GeneralPredictor(),
              violencePredictor = ViolencePredictor(),
              riskOfSeriousRecidivism = RiskOfSeriousRecidivism(),
              sexualPredictor = SexualPredictor(),
            ),
            RiskPredictorScore(
              completedDate = LocalDateTime.parse("2026-01-16T16:22:54"),
              assessmentStatus = "COMPLETE",
              groupReconviction = GroupReconviction(scoreLevel = "HIGH", score = BigDecimal(1)),
              generalPredictor = GeneralPredictor(scoreLevel = "LOW", score = BigDecimal("11.12")),
              violencePredictor = ViolencePredictor(scoreLevel = "MEDIUM", score = BigDecimal("6.1")),
              riskOfSeriousRecidivism = RiskOfSeriousRecidivism(scoreLevel = "LOW", score = BigDecimal("12.17")),
              sexualPredictor = SexualPredictor(indecentScoreLevel = "LOW", contactScoreLevel = "MEDIUM", contactScore = BigDecimal("16.2"), indecentScore = BigDecimal("15.2")),
              assessmentVersion = 1,
              allReoffendingPredictor = null,
              violentReoffendingPredictor = null,
              seriousViolentReoffendingPredictor = null,
              directContactSexualReoffendingPredictor = null,
              indirectImageContactSexualReoffendingPredictor = null,
              combinedSeriousReoffendingPredictor = null,
            ),
          ),
        )
      }

      it("returns error for the matching CRN for unknown version number") {
        assessRisksAndNeedsApiMockServer.stubForGet(
          pathNewV2,
          """
            [
              {
                "completedDate": "2025-10-23T03:02:59",
                "source": "OASYS",
                "status": "COMPLETE",
                "outputVersion": "3",
                "output": {
                }
              }
            ]
          """,
        )

        val exception =
          assertThrows<RuntimeException> {
            assessRisksAndNeedsGateway.getRiskPredictorScoresForPerson(deliusCrnNewV2)
          }
        exception.message.shouldBe("Version not supported: 3")
      }

      it("returns an empty list when no risk predictor scores are found") {
        assessRisksAndNeedsApiMockServer.stubForGet(pathNewV1, "[]")

        val response = assessRisksAndNeedsGateway.getRiskPredictorScoresForPerson(deliusCrnNewV1)

        response.data.shouldBe(emptyList())
      }

      it("returns a 404 NOT FOUND status code when no person is found") {
        assessRisksAndNeedsApiMockServer.stubForGet(pathNewV1, "", HttpStatus.NOT_FOUND)

        val response = assessRisksAndNeedsGateway.getRiskPredictorScoresForPerson(deliusCrnNewV1)

        response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND).shouldBeTrue()
      }

      it("returns a 403 FORBIDDEN status code when forbidden") {
        assessRisksAndNeedsApiMockServer.stubForGet(pathNewV1, "", HttpStatus.FORBIDDEN)

        val response = assessRisksAndNeedsGateway.getRiskPredictorScoresForPerson(deliusCrnNewV1)

        response.hasError(UpstreamApiError.Type.FORBIDDEN).shouldBeTrue()
      }
    },
  )
