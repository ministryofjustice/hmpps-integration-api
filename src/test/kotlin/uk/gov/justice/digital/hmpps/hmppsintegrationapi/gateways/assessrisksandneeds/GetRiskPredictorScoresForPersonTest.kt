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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.USE_NEW_RISK_SCORE_API
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
      val deliusCrnNewV1 = "X777777"
      val deliusCrnNewV2 = "X777778"
      val deliusCrnNewV1AndV2 = "X777779"
      val path = "/risks/predictors/$deliusCrn"
      val pathNewV1 = "/risks/predictors/unsafe/all/CRN/$deliusCrnNewV1"
      val pathNewV2 = "/risks/predictors/unsafe/all/CRN/$deliusCrnNewV2"
      var pathNewV1AndV2 = "/risks/predictors/unsafe/all/CRN/$deliusCrnNewV1AndV2"
      val assessRisksAndNeedsApiMockServer = ApiMockServer.create(UpstreamApi.ASSESS_RISKS_AND_NEEDS)

      beforeEach {
        assessRisksAndNeedsApiMockServer.start()
        Mockito.reset(hmppsAuthGateway)
        Mockito.reset(featureFlag)
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

        assessRisksAndNeedsApiMockServer.stubForGet(
          pathNewV1,
          """
            [
              {
                "completedDate": "2025-10-23T03:02:59",
                "source": "OASYS",
                "status": "COMPLETE",
                "outputVersion": "1",
                "output": {
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
              }
            ]
          """,
        )

        assessRisksAndNeedsApiMockServer.stubForGet(
          pathNewV2,
          """
            [
              {
                "completedDate": "2025-10-23T03:02:59",
                "source": "OASYS",
                "status": "COMPLETE",
                "outputVersion": "2",
                "output": {
                  "allReoffendingPredictor": {
                    "band": "LOW"
                  },
                  "violentReoffendingPredictor": {
                    "band": "MEDIUM"
                  },
                  "seriousViolentReoffendingPredictor": {
                    "band": "HIGH"
                  },
                  "directContactSexualReoffendingPredictor": {
                    "band": "LOW"
                  },
                  "indirectImageContactSexualReoffendingPredictor": {
                    "band": "LOW"
                  },
                  "combinedSeriousReoffendingPredictor": {
                    "band": "LOW"
                  }
                }
              }
            ]
          """,
        )

        assessRisksAndNeedsApiMockServer.stubForGet(
          pathNewV1AndV2,
          """
            [
            {
                "completedDate": "2025-10-23T03:02:59",
                "source": "OASYS",
                "status": "COMPLETE",
                "outputVersion": "1",
                "output": {
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
              },
              {
                "completedDate": "2025-10-23T03:02:59",
                "source": "OASYS",
                "status": "COMPLETE",
                "outputVersion": "2",
                "output": {
                  "allReoffendingPredictor": {
                    "band": "LOW"
                  },
                  "violentReoffendingPredictor": {
                    "band": "MEDIUM"
                  },
                  "seriousViolentReoffendingPredictor": {
                    "band": "HIGH"
                  },
                  "directContactSexualReoffendingPredictor": {
                    "band": "LOW"
                  },
                  "indirectImageContactSexualReoffendingPredictor": {
                    "band": "LOW"
                  },
                  "combinedSeriousReoffendingPredictor": {
                    "band": "LOW"
                  }
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

      it("returns risk predictor scores for the matching CRN with new risk score api is enabled version 1") {
        whenever(featureFlag.isEnabled(USE_NEW_RISK_SCORE_API)).thenReturn(true)

        val response = assessRisksAndNeedsGateway.getRiskPredictorScoresForPerson(deliusCrnNewV1)

        response.data.shouldBe(
          listOf(
            RiskPredictorScore(
              completedDate = LocalDateTime.parse("2025-10-23T03:02:59"),
              assessmentStatus = "COMPLETE",
              groupReconviction = GroupReconviction(scoreLevel = "HIGH"),
              generalPredictor = GeneralPredictor(scoreLevel = "LOW"),
              violencePredictor = ViolencePredictor(scoreLevel = "MEDIUM"),
              riskOfSeriousRecidivism = RiskOfSeriousRecidivism(scoreLevel = "VERY_HIGH"),
              sexualPredictor = SexualPredictor(indecentScoreLevel = "HIGH", contactScoreLevel = "VERY_HIGH"),
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

      it("returns risk predictor scores for the matching CRN with new risk score api is enabled version 2") {
        whenever(featureFlag.isEnabled(USE_NEW_RISK_SCORE_API)).thenReturn(true)

        val response = assessRisksAndNeedsGateway.getRiskPredictorScoresForPerson(deliusCrnNewV2)

        response.data.shouldBe(
          listOf(
            RiskPredictorScore(
              completedDate = LocalDateTime.parse("2025-10-23T03:02:59"),
              assessmentStatus = "COMPLETE",
              assessmentVersion = 2,
              allReoffendingPredictor = RiskScoreV2(band = "LOW"),
              violentReoffendingPredictor = RiskScoreV2(band = "MEDIUM"),
              seriousViolentReoffendingPredictor = RiskScoreV2(band = "HIGH"),
              directContactSexualReoffendingPredictor = RiskScoreV2(band = "LOW"),
              indirectImageContactSexualReoffendingPredictor = RiskScoreV2(band = "LOW"),
              combinedSeriousReoffendingPredictor = RiskScoreV2(band = "LOW"),
              groupReconviction = GroupReconviction(scoreLevel = null),
              generalPredictor = GeneralPredictor(scoreLevel = null),
              violencePredictor = ViolencePredictor(scoreLevel = null),
              riskOfSeriousRecidivism = RiskOfSeriousRecidivism(scoreLevel = null),
              sexualPredictor = SexualPredictor(indecentScoreLevel = null, contactScoreLevel = null),
            ),
          ),
        )
      }

      it("returns risk predictor scores for the matching CRN with new risk score api is enabled version 1 and version 2") {
        whenever(featureFlag.isEnabled(USE_NEW_RISK_SCORE_API)).thenReturn(true)

        val response = assessRisksAndNeedsGateway.getRiskPredictorScoresForPerson(deliusCrnNewV1AndV2)

        response.data.shouldBe(
          listOf(
            RiskPredictorScore(
              completedDate = LocalDateTime.parse("2025-10-23T03:02:59"),
              assessmentStatus = "COMPLETE",
              groupReconviction = GroupReconviction(scoreLevel = "HIGH"),
              generalPredictor = GeneralPredictor(scoreLevel = "LOW"),
              violencePredictor = ViolencePredictor(scoreLevel = "MEDIUM"),
              riskOfSeriousRecidivism = RiskOfSeriousRecidivism(scoreLevel = "VERY_HIGH"),
              sexualPredictor = SexualPredictor(indecentScoreLevel = "HIGH", contactScoreLevel = "VERY_HIGH"),
              assessmentVersion = 1,
              allReoffendingPredictor = null,
              violentReoffendingPredictor = null,
              seriousViolentReoffendingPredictor = null,
              directContactSexualReoffendingPredictor = null,
              indirectImageContactSexualReoffendingPredictor = null,
              combinedSeriousReoffendingPredictor = null,
            ),
            RiskPredictorScore(
              completedDate = LocalDateTime.parse("2025-10-23T03:02:59"),
              assessmentStatus = "COMPLETE",
              assessmentVersion = 2,
              allReoffendingPredictor = RiskScoreV2(band = "LOW"),
              violentReoffendingPredictor = RiskScoreV2(band = "MEDIUM"),
              seriousViolentReoffendingPredictor = RiskScoreV2(band = "HIGH"),
              directContactSexualReoffendingPredictor = RiskScoreV2(band = "LOW"),
              indirectImageContactSexualReoffendingPredictor = RiskScoreV2(band = "LOW"),
              combinedSeriousReoffendingPredictor = RiskScoreV2(band = "LOW"),
              groupReconviction = GroupReconviction(scoreLevel = null),
              generalPredictor = GeneralPredictor(scoreLevel = null),
              violencePredictor = ViolencePredictor(scoreLevel = null),
              riskOfSeriousRecidivism = RiskOfSeriousRecidivism(scoreLevel = null),
              sexualPredictor = SexualPredictor(indecentScoreLevel = null, contactScoreLevel = null),
            ),
          ),
        )
      }

      it("returns error for the matching CRN with new risk score api is enabled for unknown version number") {
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
        whenever(featureFlag.isEnabled(USE_NEW_RISK_SCORE_API)).thenReturn(true)

        val exception =
          assertThrows<RuntimeException> {
            assessRisksAndNeedsGateway.getRiskPredictorScoresForPerson(deliusCrnNewV2)
          }
        exception.message.shouldBe("Version not supported: 3")
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

      it("returns an empty list when no risk predictor scores are found with new risk score api is enabled") {
        assessRisksAndNeedsApiMockServer.stubForGet(pathNewV1, "[]")
        whenever(featureFlag.isEnabled(USE_NEW_RISK_SCORE_API)).thenReturn(true)

        val response = assessRisksAndNeedsGateway.getRiskPredictorScoresForPerson(deliusCrnNewV1)

        response.data.shouldBe(emptyList())
      }

      it("returns a 404 NOT FOUND status code when no person is found with new risk score api is enabled") {
        assessRisksAndNeedsApiMockServer.stubForGet(pathNewV1, "", HttpStatus.NOT_FOUND)
        whenever(featureFlag.isEnabled(USE_NEW_RISK_SCORE_API)).thenReturn(true)

        val response = assessRisksAndNeedsGateway.getRiskPredictorScoresForPerson(deliusCrnNewV1)

        response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND).shouldBeTrue()
      }

      it("returns a 403 FORBIDDEN status code when forbidden with new risk score api is enabled") {
        assessRisksAndNeedsApiMockServer.stubForGet(pathNewV1, "", HttpStatus.FORBIDDEN)
        whenever(featureFlag.isEnabled(USE_NEW_RISK_SCORE_API)).thenReturn(true)

        val response = assessRisksAndNeedsGateway.getRiskPredictorScoresForPerson(deliusCrnNewV1)

        response.hasError(UpstreamApiError.Type.FORBIDDEN).shouldBeTrue()
      }
    },
  )
