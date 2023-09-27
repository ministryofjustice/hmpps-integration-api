package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds.RiskSummary as ArnRiskSummary

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
  },
)
