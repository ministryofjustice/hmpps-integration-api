package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.math.BigDecimal
import java.time.LocalDateTime

class RiskPredictorScoreTest :
  DescribeSpec(
    {
      describe("#toRiskPredictorScore") {
        it("maps ARN Risk Predictor Score to integration API Risk Predictor Score v1") {
          val arnRiskPredictorScore =
            ArnRiskPredictorScore(
              completedDate = "2023-09-05T10:15:41",
              status = "COMPLETE",
              outputVersion = "1",
              output =
                ArnOutput(
                  generalPredictorScore = ArnGeneralPredictorScore(ogpRisk = "MEDIUM", ogp2Year = BigDecimal(2)),
                  violencePredictorScore = ArnViolencePredictorScore(ovpRisk = "LOW", twoYears = BigDecimal(2)),
                  groupReconvictionScore = ArnGroupReconvictionScore(scoreLevel = "VERY_HIGH", twoYears = BigDecimal(3)),
                  riskOfSeriousRecidivismScore = ArnRiskOfSeriousRecidivismScore(scoreLevel = "HIGH", percentageScore = BigDecimal(4)),
                  sexualPredictorScore = ArnSexualPredictorScore(ospIndecentScoreLevel = "HIGH", ospContactScoreLevel = "VERY_HIGH", ospDirectContactPercentageScore = BigDecimal(5), ospIndirectImagePercentageScore = BigDecimal(6)),
                ),
            )

          val integrationApiRiskPredictorScore = arnRiskPredictorScore.toRiskPredictorScore()

          integrationApiRiskPredictorScore.completedDate.shouldBe(LocalDateTime.parse(arnRiskPredictorScore.completedDate))
          integrationApiRiskPredictorScore.assessmentStatus.shouldBe(arnRiskPredictorScore.status)
          integrationApiRiskPredictorScore.assessmentVersion.shouldBe(arnRiskPredictorScore.outputVersion.toIntOrNull())

          integrationApiRiskPredictorScore.generalPredictor?.scoreLevel.shouldBe(
            arnRiskPredictorScore.output.generalPredictorScore.ogpRisk,
          )
          integrationApiRiskPredictorScore.generalPredictor?.score.shouldBe(
            arnRiskPredictorScore.output.generalPredictorScore.ogp2Year,
          )
          integrationApiRiskPredictorScore.violencePredictor?.scoreLevel.shouldBe(
            arnRiskPredictorScore.output.violencePredictorScore.ovpRisk,
          )
          integrationApiRiskPredictorScore.violencePredictor?.score.shouldBe(
            arnRiskPredictorScore.output.violencePredictorScore.twoYears,
          )
          integrationApiRiskPredictorScore.groupReconviction?.scoreLevel.shouldBe(
            arnRiskPredictorScore.output.groupReconvictionScore.scoreLevel,
          )
          integrationApiRiskPredictorScore.groupReconviction?.score.shouldBe(
            arnRiskPredictorScore.output.groupReconvictionScore.twoYears,
          )
          integrationApiRiskPredictorScore.riskOfSeriousRecidivism?.scoreLevel.shouldBe(
            arnRiskPredictorScore.output.riskOfSeriousRecidivismScore.scoreLevel,
          )
          integrationApiRiskPredictorScore.riskOfSeriousRecidivism?.score.shouldBe(
            arnRiskPredictorScore.output.riskOfSeriousRecidivismScore.percentageScore,
          )
          integrationApiRiskPredictorScore.sexualPredictor?.indecentScoreLevel.shouldBe(
            arnRiskPredictorScore.output.sexualPredictorScore.ospIndecentScoreLevel,
          )
          integrationApiRiskPredictorScore.sexualPredictor?.indecentScore.shouldBe(
            arnRiskPredictorScore.output.sexualPredictorScore.ospIndirectImagePercentageScore,
          )
          integrationApiRiskPredictorScore.sexualPredictor?.contactScoreLevel.shouldBe(
            arnRiskPredictorScore.output.sexualPredictorScore.ospContactScoreLevel,
          )
          integrationApiRiskPredictorScore.sexualPredictor?.contactScore.shouldBe(
            arnRiskPredictorScore.output.sexualPredictorScore.ospDirectContactPercentageScore,
          )
        }

        it("maps ARN Risk Predictor Score to integration API Risk Predictor Score v2") {
          val arnRiskPredictorScore =
            ArnRiskPredictorScore(
              completedDate = "2023-09-05T10:15:41",
              status = "COMPLETE",
              outputVersion = "2",
              output =
                ArnOutput(
                  allReoffendingPredictor = ArnScore(band = "LOW", score = BigDecimal(7)),
                  violentReoffendingPredictor = ArnScore(band = "MEDIUM", score = BigDecimal(8)),
                  seriousViolentReoffendingPredictor = ArnScore(band = "HIGH", score = BigDecimal(9)),
                  directContactSexualReoffendingPredictor = ArnScore(band = "VERY_HIGH", score = BigDecimal(10)),
                  indirectImageContactSexualReoffendingPredictor = ArnScore(band = "LOW", score = BigDecimal(11)),
                  combinedSeriousReoffendingPredictor = ArnScore(band = "MEDIUM", score = BigDecimal(12)),
                ),
            )

          val integrationApiRiskPredictorScore = arnRiskPredictorScore.toRiskPredictorScore()

          integrationApiRiskPredictorScore.completedDate.shouldBe(LocalDateTime.parse(arnRiskPredictorScore.completedDate))
          integrationApiRiskPredictorScore.assessmentStatus.shouldBe(arnRiskPredictorScore.status)
          integrationApiRiskPredictorScore.assessmentVersion.shouldBe(arnRiskPredictorScore.outputVersion.toIntOrNull())

          integrationApiRiskPredictorScore.allReoffendingPredictor?.band.shouldBe(
            arnRiskPredictorScore.output.allReoffendingPredictor.band,
          )
          integrationApiRiskPredictorScore.allReoffendingPredictor?.score.shouldBe(
            arnRiskPredictorScore.output.allReoffendingPredictor.score,
          )
          integrationApiRiskPredictorScore.violentReoffendingPredictor?.band.shouldBe(
            arnRiskPredictorScore.output.violentReoffendingPredictor.band,
          )
          integrationApiRiskPredictorScore.violentReoffendingPredictor?.score.shouldBe(
            arnRiskPredictorScore.output.violentReoffendingPredictor.score,
          )
          integrationApiRiskPredictorScore.seriousViolentReoffendingPredictor?.band.shouldBe(
            arnRiskPredictorScore.output.seriousViolentReoffendingPredictor.band,
          )
          integrationApiRiskPredictorScore.seriousViolentReoffendingPredictor?.score.shouldBe(
            arnRiskPredictorScore.output.seriousViolentReoffendingPredictor.score,
          )
          integrationApiRiskPredictorScore.directContactSexualReoffendingPredictor?.band.shouldBe(
            arnRiskPredictorScore.output.directContactSexualReoffendingPredictor.band,
          )
          integrationApiRiskPredictorScore.directContactSexualReoffendingPredictor?.score.shouldBe(
            arnRiskPredictorScore.output.directContactSexualReoffendingPredictor.score,
          )
          integrationApiRiskPredictorScore.indirectImageContactSexualReoffendingPredictor?.band.shouldBe(
            arnRiskPredictorScore.output.indirectImageContactSexualReoffendingPredictor.band,
          )
          integrationApiRiskPredictorScore.indirectImageContactSexualReoffendingPredictor?.score.shouldBe(
            arnRiskPredictorScore.output.indirectImageContactSexualReoffendingPredictor.score,
          )
          integrationApiRiskPredictorScore.combinedSeriousReoffendingPredictor?.band.shouldBe(
            arnRiskPredictorScore.output.combinedSeriousReoffendingPredictor.band,
          )
          integrationApiRiskPredictorScore.combinedSeriousReoffendingPredictor?.score.shouldBe(
            arnRiskPredictorScore.output.combinedSeriousReoffendingPredictor.score,
          )
        }

        it("maps ARN Risk Predictor Score to integration API Risk Predictor Score v2 sending decimals") {
          val arnRiskPredictorScore =
            ArnRiskPredictorScore(
              completedDate = "2023-09-05T10:15:41",
              status = "COMPLETE",
              outputVersion = "2",
              output =
                ArnOutput(
                  allReoffendingPredictor = ArnScore(band = "LOW", score = BigDecimal("7.1")),
                  violentReoffendingPredictor = ArnScore(band = "MEDIUM", score = BigDecimal("8.1")),
                  seriousViolentReoffendingPredictor = ArnScore(band = "HIGH", score = BigDecimal("9.24")),
                  directContactSexualReoffendingPredictor = ArnScore(band = "VERY_HIGH", score = BigDecimal("10.2")),
                  indirectImageContactSexualReoffendingPredictor = ArnScore(band = "LOW", score = BigDecimal("11.28")),
                  combinedSeriousReoffendingPredictor = ArnScore(band = "MEDIUM", score = BigDecimal("12.13")),
                ),
            )

          val integrationApiRiskPredictorScore = arnRiskPredictorScore.toRiskPredictorScore(sendDecimals = true)

          integrationApiRiskPredictorScore.completedDate.shouldBe(LocalDateTime.parse(arnRiskPredictorScore.completedDate))
          integrationApiRiskPredictorScore.assessmentStatus.shouldBe(arnRiskPredictorScore.status)
          integrationApiRiskPredictorScore.assessmentVersion.shouldBe(arnRiskPredictorScore.outputVersion.toIntOrNull())

          integrationApiRiskPredictorScore.allReoffendingPredictor?.band.shouldBe(
            arnRiskPredictorScore.output.allReoffendingPredictor.band,
          )
          integrationApiRiskPredictorScore.allReoffendingPredictor?.score.shouldBe(
            arnRiskPredictorScore.output.allReoffendingPredictor.score,
          )
          integrationApiRiskPredictorScore.violentReoffendingPredictor?.band.shouldBe(
            arnRiskPredictorScore.output.violentReoffendingPredictor.band,
          )
          integrationApiRiskPredictorScore.violentReoffendingPredictor?.score.shouldBe(
            arnRiskPredictorScore.output.violentReoffendingPredictor.score,
          )
          integrationApiRiskPredictorScore.seriousViolentReoffendingPredictor?.band.shouldBe(
            arnRiskPredictorScore.output.seriousViolentReoffendingPredictor.band,
          )
          integrationApiRiskPredictorScore.seriousViolentReoffendingPredictor?.score.shouldBe(
            arnRiskPredictorScore.output.seriousViolentReoffendingPredictor.score,
          )
          integrationApiRiskPredictorScore.directContactSexualReoffendingPredictor?.band.shouldBe(
            arnRiskPredictorScore.output.directContactSexualReoffendingPredictor.band,
          )
          integrationApiRiskPredictorScore.directContactSexualReoffendingPredictor?.score.shouldBe(
            arnRiskPredictorScore.output.directContactSexualReoffendingPredictor.score,
          )
          integrationApiRiskPredictorScore.indirectImageContactSexualReoffendingPredictor?.band.shouldBe(
            arnRiskPredictorScore.output.indirectImageContactSexualReoffendingPredictor.band,
          )
          integrationApiRiskPredictorScore.indirectImageContactSexualReoffendingPredictor?.score.shouldBe(
            arnRiskPredictorScore.output.indirectImageContactSexualReoffendingPredictor.score,
          )
          integrationApiRiskPredictorScore.combinedSeriousReoffendingPredictor?.band.shouldBe(
            arnRiskPredictorScore.output.combinedSeriousReoffendingPredictor.band,
          )
          integrationApiRiskPredictorScore.combinedSeriousReoffendingPredictor?.score.shouldBe(
            arnRiskPredictorScore.output.combinedSeriousReoffendingPredictor.score,
          )
        }
      }
    },
  )
