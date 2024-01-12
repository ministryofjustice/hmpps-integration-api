package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class RiskSummaryTest : DescribeSpec(
  {
    describe("#toRiskSummary") {
      it("maps one-to-one attributes to Integration API attributes") {
        val arnRiskSummary = ArnRiskSummary(
          whoIsAtRisk = "X, Y and Z are at risk",
          natureOfRisk = "The nature of the risk is X",
          riskImminence = "the risk is imminent and more probably in X situation",
          riskIncreaseFactors = "If offender in situation X the risk can be higher",
          riskMitigationFactors = "Giving offender therapy in X will reduce the risk",
          overallRiskLevel = "HIGH",
        )

        val integrationApiRiskSummary = arnRiskSummary.toRiskSummary()

        integrationApiRiskSummary.whoIsAtRisk.shouldBe(arnRiskSummary.whoIsAtRisk)
        integrationApiRiskSummary.natureOfRisk.shouldBe(arnRiskSummary.natureOfRisk)
        integrationApiRiskSummary.riskImminence.shouldBe(arnRiskSummary.riskImminence)
        integrationApiRiskSummary.riskIncreaseFactors.shouldBe(arnRiskSummary.riskIncreaseFactors)
        integrationApiRiskSummary.riskMitigationFactors.shouldBe(arnRiskSummary.riskMitigationFactors)
        integrationApiRiskSummary.overallRiskLevel.shouldBe(arnRiskSummary.overallRiskLevel)
      }
    }

    it("maps ARN Risk in Community to Integration API Risk in Community") {
      val arnRiskSummary = ArnRiskSummary(
        riskInCommunity = mapOf(
          "HIGH" to listOf("Children", "Public", "Known adult"),
          "MEDIUM" to listOf("Staff"),
          "LOW" to listOf("Prisoners"),
        ),
      )

      val integrationApiRiskSummary = arnRiskSummary.toRiskSummary()

      integrationApiRiskSummary.riskInCommunity.shouldBe(
        mapOf(
          "children" to "HIGH",
          "public" to "HIGH",
          "knownAdult" to "HIGH",
          "staff" to "MEDIUM",
          "prisoners" to "LOW",
        ),
      )
    }

    it("maps ARN Risk in Custody to Integration API Risk in Custody") {
      val arnRiskSummary = ArnRiskSummary(
        riskInCustody = mapOf(
          "VERY_HIGH" to listOf("Known adult"),
          "HIGH" to listOf("Children"),
          "MEDIUM" to listOf("Staff", "Public"),
          "LOW" to listOf("Prisoners"),
        ),
      )

      val integrationApiRiskSummary = arnRiskSummary.toRiskSummary()

      integrationApiRiskSummary.riskInCustody.shouldBe(
        mapOf(
          "children" to "HIGH",
          "public" to "MEDIUM",
          "knownAdult" to "VERY_HIGH",
          "staff" to "MEDIUM",
          "prisoners" to "LOW",
        ),
      )
    }
  },
)
