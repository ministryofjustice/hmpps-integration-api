package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class OtherRisksTest : DescribeSpec(
  {
    describe("#toOtherRisks") {
      it("maps one-to-one attributes to Integration API attributes") {
        val arnOtherRisks =
          ArnOtherRisks(
            escapeOrAbscond = "YES",
            controlIssuesDisruptiveBehaviour = "YES",
            breachOfTrust = "YES",
            riskToOtherPrisoners = "YES",
          )

        val integrationApiOtherRisks = arnOtherRisks.toOtherRisks()

        integrationApiOtherRisks.escapeOrAbscond.shouldBe(arnOtherRisks.escapeOrAbscond)
        integrationApiOtherRisks.controlIssuesDisruptiveBehaviour.shouldBe(arnOtherRisks.controlIssuesDisruptiveBehaviour)
        integrationApiOtherRisks.breachOfTrust.shouldBe(arnOtherRisks.breachOfTrust)
        integrationApiOtherRisks.riskToOtherPrisoners.shouldBe(arnOtherRisks.riskToOtherPrisoners)
      }
    }
  },
)
