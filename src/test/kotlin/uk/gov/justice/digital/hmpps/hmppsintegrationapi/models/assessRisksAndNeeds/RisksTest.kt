package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.OtherRisks
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Risk
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.RiskSummary
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.RiskToSelf
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Risks
import java.time.LocalDateTime

class RisksTest : DescribeSpec(
  {
    describe("#toRisks") {
      it("maps one-to-one attributes to Integration API attributes") {
        val arnRisks =
          ArnRisks(
            assessedOn = LocalDateTime.now(),
            riskToSelf =
              ArnRiskToSelf(
                suicide = ArnRisk(risk = "NO"),
                selfHarm = ArnRisk(risk = "NO"),
                custody = ArnRisk(risk = "NO"),
                hostelSetting = ArnRisk(risk = "NO"),
                vulnerability = ArnRisk(risk = "NO"),
              ),
            otherRisks = ArnOtherRisks(breachOfTrust = "YES"),
            summary =
              ArnRiskSummary(
                whoIsAtRisk = "X, Y and Z are at risk",
              ),
          )

        val integrationApiRisks = arnRisks.toRisks()

        integrationApiRisks.assessedOn.shouldBe(arnRisks.assessedOn)
        integrationApiRisks.riskToSelf.suicide.risk.shouldBe(arnRisks.riskToSelf.suicide.risk)
        integrationApiRisks.riskToSelf.selfHarm.risk.shouldBe(arnRisks.riskToSelf.selfHarm.risk)
        integrationApiRisks.riskToSelf.custody.risk.shouldBe(arnRisks.riskToSelf.custody.risk)
        integrationApiRisks.riskToSelf.hostelSetting.risk.shouldBe(arnRisks.riskToSelf.hostelSetting.risk)
        integrationApiRisks.riskToSelf.vulnerability.risk.shouldBe(arnRisks.riskToSelf.vulnerability.risk)
        integrationApiRisks.otherRisks.breachOfTrust.shouldBe(arnRisks.otherRisks.breachOfTrust)
        integrationApiRisks.summary.whoIsAtRisk.shouldBe(arnRisks.summary.whoIsAtRisk)
      }

      it("maps Risk in Community and Risk in Custody to Integration API attributes") {
        val arnRisks =
          ArnRisks(
            summary =
              ArnRiskSummary(
                riskInCommunity =
                  mapOf(
                    "HIGH" to listOf("Children", "Public", "Known adult"),
                    "MEDIUM" to listOf("Staff"),
                    "LOW" to listOf("Prisoners"),
                  ),
                riskInCustody =
                  mapOf(
                    "VERY_HIGH" to listOf("Known adult"),
                    "HIGH" to listOf("Children"),
                    "MEDIUM" to listOf("Staff", "Public"),
                    "LOW" to listOf("Prisoners"),
                  ),
              ),
          )

        val integrationApiRisks = arnRisks.toRisks()

        integrationApiRisks.summary.riskInCommunity.shouldBe(
          mapOf(
            "children" to "HIGH",
            "public" to "HIGH",
            "knownAdult" to "HIGH",
            "staff" to "MEDIUM",
            "prisoners" to "LOW",
          ),
        )

        integrationApiRisks.summary.riskInCustody.shouldBe(
          mapOf(
            "children" to "HIGH",
            "public" to "MEDIUM",
            "knownAdult" to "VERY_HIGH",
            "staff" to "MEDIUM",
            "prisoners" to "LOW",
          ),
        )
      }

      it("handles null values") {
        val arnRisks = ArnRisks()

        val integrationApiRisks = arnRisks.toRisks()

        integrationApiRisks.shouldBe(
          Risks(
            assessedOn = null,
            riskToSelf =
              RiskToSelf(
                suicide = Risk(),
                selfHarm = Risk(),
                custody = Risk(),
                hostelSetting = Risk(),
                vulnerability = Risk(),
              ),
            otherRisks = OtherRisks(),
            summary = RiskSummary(),
          ),
        )
      }
    }
  },
)
